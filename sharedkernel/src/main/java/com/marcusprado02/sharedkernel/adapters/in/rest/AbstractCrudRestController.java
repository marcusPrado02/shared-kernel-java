package com.marcusprado02.sharedkernel.adapters.in.rest;


import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.marcusprado02.sharedkernel.application.service.CrudApplicationService;
import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;
import com.marcusprado02.sharedkernel.contracts.api.PageResponse;
import com.marcusprado02.sharedkernel.contracts.api.SearchQuery;
import com.marcusprado02.sharedkernel.infrastructure.platform.http.ETagService;
import com.marcusprado02.sharedkernel.infrastructure.platform.http.SparseFields;
import com.marcusprado02.sharedkernel.infrastructure.platform.idempotency.IdempotencyService;

import java.time.Duration;
import java.util.*;

@Timed("http.api")
public abstract class AbstractCrudRestController<RES, CREATE, UPDATE, ID> {

    protected final CrudApplicationService<RES, CREATE, UPDATE, ID> service;
    protected final IdempotencyService idempotency;
    protected final ETagService etags;

    @Autowired
    protected AbstractCrudRestController(CrudApplicationService<RES, CREATE, UPDATE, ID> service,
                                         IdempotencyService idempotency,
                                         ETagService etags) {
        this.service = service;
        this.idempotency = idempotency;
        this.etags = etags;
    }

    /** Base path do recurso, ex: "/api/v1/users" (para Location/links). */
    protected abstract String resourcePath();

    /** Resolve ID a partir do DTO criado (para montar Location); override se necessário. */
    protected abstract ID extractIdFromResource(RES res);

    /** GET /{id} — com ETag/If-None-Match e sparse fields */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable @NotNull ID id,
                                     @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
                                     @RequestParam(value = "fields", required = false) String fields) {

        var resOpt = service.findById(id);
        if (resOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        var version = service.versionOf(id).orElseGet(() -> etags.strongETag(resOpt.get()));
        var etag = version.startsWith("\"") ? version : etags.strongETagFromVersion(version);

        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        var body = SparseFields.filter(
            new ApiResponse<RES>(
                resOpt.get(),
                Map.of("version", version),
                Map.of(),
                null,
                200,
                Map.of(),
                null,
                null
            ),
            fields
        );
        return ResponseEntity.ok().eTag(etag).cacheControl(CacheControl.noCache()).body(body);
    }

    /** GET / — paginação, ordenação e filtros arbitrários */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<RES>> search(
            @ParameterObject Pageable pageable,   // ex: ?page=0&size=20&sort=createdAt,desc
            @RequestParam MultiValueMap<String, String> filters,
            @RequestParam(value = "fields", required = false) String fields,
            UriComponentsBuilder uri) {

        var query = SearchQuery.of(filters, fields);
        Page<RES> page = service.search(query, safePageable(pageable));

        var links = paginationLinks(uri.path(resourcePath()).build().toUriString(), page.getNumber(), page.getSize(), page.getTotalPages(), filters);
        var resp = new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize(), links);

        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(resp);
    }

    /** POST / — cria com idempotência via header Idempotency-Key */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RES>> create(@Valid @RequestBody CREATE cmd,
                                                   @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                                   UriComponentsBuilder uri) {

        String key = (idemKey == null || idemKey.isBlank()) ? UUID.randomUUID().toString() : idemKey;

        RES created = idempotency.withIdempotency(key, Duration.ofHours(24), () -> service.create(cmd));
        var id = extractIdFromResource(created);
        var version = service.versionOf(id).orElseGet(() -> etags.strongETag(created));
        var etag = version.startsWith("\"") ? version : etags.strongETagFromVersion(version);

        var location = uri.path(resourcePath()).pathSegment(String.valueOf(id)).build().toUri();
        var body = new ApiResponse<>(
            created,
            Map.of("version", version, "idempotencyKey", key),
            Map.of("self", location.toString()),
            null,
            201,
            Map.of(),
            null,
            null
        );

        return ResponseEntity.created(location).eTag(etag).cacheControl(CacheControl.noCache()).body(body);
    }

    /** PUT /{id} — atualização completa com If-Match (otimismo) */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RES>> update(@PathVariable ID id,
                                                   @Valid @RequestBody UPDATE cmd,
                                                   @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        // Se o cliente enviou If-Match, o serviço pode validar a versão para evitar lost update
        if (ifMatch != null && !ifMatch.isBlank()) {
            var current = service.versionOf(id).orElse(null);
            if (current != null) {
                var currentEtag = current.startsWith("\"") ? current : etags.strongETagFromVersion(current);
                if (!Objects.equals(currentEtag, ifMatch)) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
                }
            }
        }

        RES updated = service.update(id, cmd);
        var version = service.versionOf(id).orElseGet(() -> etags.strongETag(updated));
        var etag = version.startsWith("\"") ? version : etags.strongETagFromVersion(version);

        var body = new ApiResponse<>(
            updated,
            Map.of("version", version),
            Map.of(),
            null,
            200,
            Map.of(),
            null,
            null
        );
        return ResponseEntity.ok().eTag(etag).cacheControl(CacheControl.noCache()).body(body);
    }

    /** PATCH /{id} — opcional (parcial). Se não tiver JSON-Patch, trate como upsert parcial no service. */
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RES>> patch(@PathVariable ID id, @Valid @RequestBody UPDATE partialCmd) {
        RES updated = service.update(id, partialCmd);
        var version = service.versionOf(id).orElseGet(() -> etags.strongETag(updated));
        var etag = version.startsWith("\"") ? version : etags.strongETagFromVersion(version);

        var body = new ApiResponse<>(
            updated,
            Map.of("version", version),
            Map.of(),
            null,
            200,
            Map.of(),
            null,
            null
        );
        return ResponseEntity.ok().eTag(etag).cacheControl(CacheControl.noCache()).body(body);
    }

    /** DELETE /{id} — 204 No Content */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------- helpers -----------------

    protected Pageable safePageable(Pageable p) {
        int page = Math.max(0, p.getPageNumber());
        int size = Math.min(Math.max(1, p.getPageSize()), 200); // anti-DoS: limita tamanho
        Sort sort = p.getSort().isSorted() ? p.getSort() : Sort.unsorted();
        return PageRequest.of(page, size, sort);
    }

    protected Map<String, String> paginationLinks(String basePath, int page, int size, int totalPages, MultiValueMap<String,String> filters) {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", buildPageUri(basePath, page, size, filters));
        links.put("first", buildPageUri(basePath, 0, size, filters));
        links.put("last", buildPageUri(basePath, Math.max(totalPages - 1, 0), size, filters));
        if (page > 0) links.put("prev", buildPageUri(basePath, page - 1, size, filters));
        if (page + 1 < totalPages) links.put("next", buildPageUri(basePath, page + 1, size, filters));
        return links;
    }

    private String buildPageUri(String base, int page, int size, MultiValueMap<String,String> filters) {
        var qp = new StringBuilder("?page=" + page + "&size=" + size);
        for (var e : filters.entrySet()) {
            for (var v : e.getValue()) qp.append("&").append(e.getKey()).append("=").append(v);
        }
        return base + qp;
    }
}
