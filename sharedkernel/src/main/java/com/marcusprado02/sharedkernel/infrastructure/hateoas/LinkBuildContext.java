package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;
import java.util.*;


/** Contexto de build (baseUri, versão, idioma, escopos). */
public record LinkBuildContext(URI baseUri, String apiVersion, Locale locale, Set<String> scopes, Map<String,Object> vars) {
    public boolean hasScope(String s){ return scopes!=null && scopes.contains(s); }
}