package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.adapter.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.FlagContext;

import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * FlagContextFactory — cria FlagContext a partir do ambiente Spring (Security + Request).
 *
 * Convenções:
 * - Campos podem ser null se não forem encontrados.
 * - A extração é “best-effort” e NÃO lança exceções em ausência de módulos (fail-safe).
 * - Cabeçalhos suportados:
 *   - Tenant: X-Tenant-Id, X-Org-Id
 *   - Região: X-Region, X-Geo-Region
 * - Atributos extras populados quando possível: authorities, userAgent, ip, remoteAddress, plan, scopes...
 */
public final class FlagContextFactory {

  private FlagContextFactory() {}

  /** Cria um contexto usando SecurityContext e HttpServletRequest atual (se existirem). */
  public static FlagContext fromSecurityContext() {
    String tenantId = null;
    String userId = null;
    String sessionId = null;
    String region = null;
    Map<String, Object> attrs = new HashMap<>();

    // 1) Spring Security (best-effort; evita NoClassDefFoundError)
    try {
      var contextClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
      var getContext = contextClass.getMethod("getContext");
      Object context = getContext.invoke(null);
      if (context != null) {
        var getAuth = context.getClass().getMethod("getAuthentication");
        Object auth = getAuth.invoke(context);
        if (auth != null) {
          // username
          try {
            var getName = auth.getClass().getMethod("getName");
            userId = safeString(getName.invoke(auth));
          } catch (ReflectiveOperationException ignored) {}

          // authorities
          try {
            var getAuthorities = auth.getClass().getMethod("getAuthorities");
            Object auths = getAuthorities.invoke(auth);
            List<String> roles = new ArrayList<>();
            if (auths instanceof Iterable<?> it) {
              for (Object ga : it) {
                try {
                  var getAuthority = ga.getClass().getMethod("getAuthority");
                  roles.add(String.valueOf(getAuthority.invoke(ga)));
                } catch (ReflectiveOperationException ignored) {}
              }
            }
            if (!roles.isEmpty()) attrs.put("authorities", roles);
          } catch (ReflectiveOperationException ignored) {}

          // details (WebAuthenticationDetails: remoteAddress, sessionId)
          try {
            var getDetails = auth.getClass().getMethod("getDetails");
            Object details = getDetails.invoke(auth);
            if (details != null && "org.springframework.security.web.authentication.WebAuthenticationDetails".equals(details.getClass().getName())) {
              var getRemote = details.getClass().getMethod("getRemoteAddress");
              var getSession = details.getClass().getMethod("getSessionId");
              attrs.put("remoteAddress", safeString(getRemote.invoke(details)));
              sessionId = firstNonBlank(sessionId, safeString(getSession.invoke(details)));
            }
          } catch (ReflectiveOperationException ignored) {}

          // principal como Jwt (claims tenant/plan/region/scp)
          try {
            var getPrincipal = auth.getClass().getMethod("getPrincipal");
            Object principal = getPrincipal.invoke(auth);
            if (principal != null && "org.springframework.security.oauth2.jwt.Jwt".equals(principal.getClass().getName())) {
              var getClaims = principal.getClass().getMethod("getClaims");
              @SuppressWarnings("unchecked")
              Map<String, Object> claims = (Map<String, Object>) getClaims.invoke(principal);
              tenantId = firstNonBlank(tenantId, asString(claims.get("tenant")), asString(claims.get("tenant_id")));
              region   = firstNonBlank(region,   asString(claims.get("region")), asString(claims.get("geo")));
              putIfNotNull(attrs, "plan", claims.get("plan"));
              putIfNotNull(attrs, "scopes", claims.get("scope")); // ou "scp" em alguns IdPs
            }
          } catch (ReflectiveOperationException ignored) {}

          // principal como UserDetails
          try {
            var getPrincipal = auth.getClass().getMethod("getPrincipal");
            Object principal = getPrincipal.invoke(auth);
            if (principal != null && implementsInterface(principal, "org.springframework.security.core.userdetails.UserDetails")) {
              var getUsername = principal.getClass().getMethod("getUsername");
              userId = firstNonBlank(userId, safeString(getUsername.invoke(principal)));
            }
          } catch (ReflectiveOperationException ignored) {}
        }
      }
    } catch (ClassNotFoundException ignored) {
      // Spring Security não presente — continuar
    } catch (ReflectiveOperationException ignored) {
      // Qualquer falha reflection — continuar
    }

    // 2) Servlet request atual
    var req = currentRequest();
    if (req != null) {
      // headers de tenant/region
      tenantId = firstNonBlank(tenantId, req.getHeader("X-Tenant-Id"), req.getHeader("X-Org-Id"));
      region   = firstNonBlank(region,   req.getHeader("X-Region"), req.getHeader("X-Geo-Region"));

      // session id
      HttpSession session = req.getSession(false);
      if (session != null) sessionId = firstNonBlank(sessionId, session.getId());

      // atributos práticos
      putIfNotBlank(attrs, "userAgent", req.getHeader("User-Agent"));
      putIfNotBlank(attrs, "ip", req.getRemoteAddr());
      putIfNotBlank(attrs, "method", req.getMethod());
      putIfNotBlank(attrs, "path", req.getRequestURI());
    }

    return new FlagContext(
        nullIfBlank(tenantId),
        nullIfBlank(userId),
        nullIfBlank(sessionId),
        nullIfBlank(region),
        attrs.isEmpty() ? Map.of() : Map.copyOf(attrs)
    );
  }

  /** Cria a partir de um HttpServletRequest específico. */
  public static FlagContext fromRequest(HttpServletRequest req) {
    String tenantId = firstNonBlank(null, req.getHeader("X-Tenant-Id"), req.getHeader("X-Org-Id"));
    String region   = firstNonBlank(null, req.getHeader("X-Region"), req.getHeader("X-Geo-Region"));
    String sessionId = null;
    HttpSession session = req.getSession(false);
    if (session != null) sessionId = session.getId();

    Map<String, Object> attrs = new HashMap<>();
    putIfNotBlank(attrs, "userAgent", req.getHeader("User-Agent"));
    putIfNotBlank(attrs, "ip", req.getRemoteAddr());
    putIfNotBlank(attrs, "method", req.getMethod());
    putIfNotBlank(attrs, "path", req.getRequestURI());

    return new FlagContext(nullIfBlank(tenantId), null, nullIfBlank(sessionId), nullIfBlank(region),
        attrs.isEmpty() ? Map.of() : Map.copyOf(attrs));
  }

  /** Constrói um contexto anônimo, apenas com atributos passados. */
  public static FlagContext anonymous(@Nullable Map<String, Object> attributes) {
    Map<String, Object> attrs = attributes == null ? Map.of() : Map.copyOf(attributes);
    return new FlagContext(null, null, null, null, attrs);
  }

  // ----------------- helpers -----------------

  @Nullable
  private static HttpServletRequest currentRequest() {
    var attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes sra) {
      return sra.getRequest();
    }
    return null;
  }

  private static boolean implementsInterface(Object o, String interfaceName) {
    for (var itf : o.getClass().getInterfaces()) {
      if (itf.getName().equals(interfaceName)) return true;
    }
    return false;
  }

  private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
    if (value != null) map.put(key, value);
  }

  private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
    if (value != null && !value.isBlank()) map.put(key, value);
  }

  private static String firstNonBlank(String current, String... candidates) {
    if (current != null && !current.isBlank()) return current;
    if (candidates == null) return null;
    for (String c : candidates) if (c != null && !c.isBlank()) return c;
    return null;
  }

  private static String asString(Object o) {
    return o == null ? null : String.valueOf(o);
  }

  private static String safeString(Object o) {
    try { return asString(o); } catch (Exception e) { return null; }
  }

  private static String nullIfBlank(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
