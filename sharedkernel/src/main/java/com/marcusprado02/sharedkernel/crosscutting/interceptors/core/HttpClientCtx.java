package com.marcusprado02.sharedkernel.crosscutting.interceptors.core;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// Feign (opcional, se você usa)
import feign.RequestTemplate;

// Spring RestTemplate
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

// OkHttp
import okhttp3.Request;
import okhttp3.Response;

/**
 * Contexto polimórfico para clientes HTTP.
 * Suporta Feign, RestTemplate e OkHttp.
 */
public final class HttpClientCtx implements InterceptionContext {

    // --- Feign
    private final RequestTemplate feignTemplate;

    // --- RestTemplate
    private final HttpRequest springRequest;
    private byte[] springBody;
    private final ClientHttpRequestExecution springExecution;

    // --- OkHttp
    private final okhttp3.Interceptor.Chain okChain;
    private final Request.Builder okBuilder;

    private HttpClientCtx(RequestTemplate feignTemplate,
                          HttpRequest springRequest, byte[] springBody, ClientHttpRequestExecution springExecution,
                          okhttp3.Interceptor.Chain okChain, Request.Builder okBuilder) {
        this.feignTemplate = feignTemplate;
        this.springRequest = springRequest; this.springBody = springBody; this.springExecution = springExecution;
        this.okChain = okChain; this.okBuilder = okBuilder;
    }

    // ---------- FACTORIES

    // Feign
    public static HttpClientCtx from(RequestTemplate template) {
        Objects.requireNonNull(template, "template");
        return new HttpClientCtx(template, null, null, null, null, null);
    }

    // RestTemplate
    public static HttpClientCtx from(HttpRequest req, byte[] body, ClientHttpRequestExecution ex) {
        Objects.requireNonNull(req, "req"); Objects.requireNonNull(ex, "execution");
        return new HttpClientCtx(null, req, body != null ? body : new byte[0], ex, null, null);
    }

    // OkHttp
    public static HttpClientCtx from(Request request, okhttp3.Interceptor.Chain chain) {
        Objects.requireNonNull(request, "request"); Objects.requireNonNull(chain, "chain");
        return new HttpClientCtx(null, null, null, null, chain, request.newBuilder());
    }

    // ---------- Leitura genérica

    public String method() {
        if (feignTemplate != null) return feignTemplate.method();
        if (springRequest != null) return springRequest.getMethod().name();
        if (okBuilder != null)     return okBuilder.build().method();
        return "UNKNOWN";
    }

    public String url() {
        if (feignTemplate != null) return feignTemplate.url();
        if (springRequest != null) return springRequest.getURI().toString();
        if (okBuilder != null)     return okBuilder.build().url().toString();
        return "";
    }

    public Map<String, Collection<String>> headers() {
        if (feignTemplate != null) return feignTemplate.headers();
        if (springRequest != null) {
            HttpHeaders h = springRequest.getHeaders();
            return h.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        }
        if (okBuilder != null) {
            Request r = okBuilder.build();
            Map<String, Collection<String>> m = new LinkedHashMap<>();
            for (String name : r.headers().names()) {
                m.put(name, r.headers(name));
            }
            return m;
        }
        return Map.of();
    }

    public byte[] body() {
        if (feignTemplate != null) return feignTemplate.body();
        if (springRequest != null) return springBody;
        // OkHttp: corpo não está acessível aqui (RequestBody), manter null
        return null;
    }

    // ---------- Mutação

    public HttpClientCtx header(String name, String value) {
        if (feignTemplate != null) feignTemplate.header(name, value);
        else if (springRequest != null) springRequest.getHeaders().add(name, value);
        else if (okBuilder != null) okBuilder.header(name, value);
        return this;
    }

    public HttpClientCtx replaceHeader(String name, String value) {
        if (feignTemplate != null) { feignTemplate.header(name); feignTemplate.header(name, value); }
        else if (springRequest != null) { springRequest.getHeaders().set(name, value); }
        else if (okBuilder != null) { okBuilder.removeHeader(name); okBuilder.addHeader(name, value); }
        return this;
    }

    public HttpClientCtx addHeaders(Map<String,String> h) { h.forEach(this::header); return this; }

    public HttpClientCtx body(byte[] newBody) {
        if (springRequest != null) this.springBody = newBody != null ? newBody : new byte[0];
        return this;
    }

    // ---------- Execução (terminal)

    /** Executa a requisição na stack subjacente e retorna a resposta apropriada. */
    public Object execute() throws Exception {
        if (feignTemplate != null) {
            // Feign RequestInterceptor não executa; mantenha no-op para compat.
            return null;
        }
        if (springRequest != null) {
            return springExecution.execute(springRequest, springBody);
        }
        if (okChain != null && okBuilder != null) {
            return okChain.proceed(okBuilder.build());
        }
        return null;
    }

    // ---------- InterceptionContext

    @Override public String operation() { return method() + " " + url(); }

    @Override public Map<String, Object> attributes() {
        Map<String,Object> m = new HashMap<>();
        m.put("headers", headers());
        m.put("url", url());
        m.put("method", method());
        return m;
    }

    @Override public Carrier carrier() {
        return new Carrier() {
            @Override public Optional<String> get(String key) {
                var hs = headers();
                if (hs == null) return Optional.empty();
                var v = hs.get(key);
                return v == null || v.isEmpty() ? Optional.empty() : Optional.ofNullable(v.iterator().next());
            }
            @Override public void set(String key, String value) { replaceHeader(key, value); }
            @Override public Map<String, String> dump() {
                Map<String,String> flat = new LinkedHashMap<>();
                headers().forEach((k,v) -> flat.put(k, v == null || v.isEmpty() ? null : v.iterator().next()));
                return flat;
            }
        };
    }

    @Override public Instant startTime() { return Instant.now(); }
}
