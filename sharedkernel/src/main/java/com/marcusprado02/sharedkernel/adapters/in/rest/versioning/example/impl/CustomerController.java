package com.marcusprado02.sharedkernel.adapters.in.rest.versioning.example.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcusprado02.sharedkernel.adapters.in.rest.versioning.ApiVersion;
import com.marcusprado02.sharedkernel.adapters.in.rest.versioning.VersionDecision;
import com.marcusprado02.sharedkernel.adapters.in.rest.versioning.VersionNegotiationFilter;
import com.marcusprado02.sharedkernel.adapters.in.rest.versioning.VersionedMapper;
import com.marcusprado02.sharedkernel.application.service.example.impl.CustomerQueryService;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.Customer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final VersionedMapper<Customer, Object> mapper;
    private final CustomerQueryService service;

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id, HttpServletRequest req){
        var decision = (VersionDecision) req.getAttribute(VersionNegotiationFilter.ATTR_DECISION);

        Customer entity = service.get(id); // ✅ agora é Customer, não Object
        Object dto;
        switch (decision.served.major()){
            case 1 -> dto = (decision.served.minor() >= 1)
                    ? mapper.v1_1(entity)
                    : mapper.v1_0(entity);
            case 2 -> dto = mapper.v2_0(entity);
            default -> throw new IllegalStateException("unsupported version");
        }
        return ResponseEntity.ok().eTag(versionedEtag(dto, decision.served)).body(dto);
    }

    private String versionedEtag(Object dto, ApiVersion v){
        return "\"W/" + v + "-" + Integer.toHexString(dto.hashCode()) + "\"";
    }
}

