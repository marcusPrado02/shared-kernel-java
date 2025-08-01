package com.marcusprado02.sharedkernel.application.bulk;

import java.util.List;
import com.marcusprado02.sharedkernel.application.command.Command;
import com.marcusprado02.sharedkernel.application.handler.command.CommandHandler;

/**
 * Handler para BulkCommand, processa cada sub-command e retorna List<R>.
 *
 * @param <B> tipo do BulkCommand (que retorna List<R>)
 * @param <C> tipo de cada comando interno (extends Command<R>)
 * @param <R> tipo de retorno de cada sub-command
 */
public interface BulkCommandHandler<B extends BulkCommand<C, R>, C extends Command<R>, R>
        extends CommandHandler<B, List<R>> {
}
