package com.marcusprado02.sharedkernel.crosscutting.auth;

import com.marcusprado02.sharedkernel.crosscutting.context.AuthContext;

public interface AuthzService {
    /** Decodifica/valida o token e retorna contexto autenticado. Pode retornar convidado/anonymous. */
    AuthContext authenticate(String bearerToken) throws Exception;

    /** Autoriza a operação (ex.: ABAC/OPA/policies). Deve lançar se negar. */
    void authorize(AuthContext ctx, String operation) throws Exception;
}
