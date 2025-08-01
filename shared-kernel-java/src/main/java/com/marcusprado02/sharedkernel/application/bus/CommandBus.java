package com.marcusprado02.sharedkernel.application.bus;

import com.marcusprado02.sharedkernel.application.command.Command;

/**
 * Despacha Commands do tipo C para o handler registrado.
 */
public interface CommandBus {
    /**
     * Despacha o comando e devolve o resultado do tipo R.
     *
     * @param command comando cujo handler retorna R
     * @param <R> tipo de retorno do comando
     * @param <C> tipo concreto do comando
     */
    <R, C extends Command<R>> R dispatch(C command);
}

