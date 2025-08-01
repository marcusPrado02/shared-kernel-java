package com.marcusprado02.sharedkernel.application.bulk;


import java.util.List;
import com.marcusprado02.sharedkernel.application.command.Command;

/**
 * Marca um comando que contém múltiplos sub‑commands de mesmo tipo.
 *
 * @param <C> Tipo de cada comando interno
 */
public interface BulkCommand<C extends Command<R>, R> extends Command<List<R>> {
    /**
     * @return lista de comandos a serem executados em lote
     */
    List<C> getCommands();
}
