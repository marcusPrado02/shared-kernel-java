package com.marcusprado02.sharedkernel.cqrs.handler;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.handler.events.DomainEventPublisher;
import com.marcusprado02.sharedkernel.cqrs.handler.outbox.OutboxService;
import com.marcusprado02.sharedkernel.cqrs.handler.security.AuthorizationService;
import com.marcusprado02.sharedkernel.cqrs.handler.tx.TransactionManager;
import com.marcusprado02.sharedkernel.cqrs.handler.validation.BusinessValidator;

public abstract class AbstractCommandHandler<C extends Command<R>, R> implements CommandHandler<C, R> {

    protected final HandlerOptions options;
    protected final TransactionManager txManager;
    protected final DomainEventPublisher eventPublisher;
    protected final OutboxService outbox;
    protected final AuthorizationService authz;
    protected final BusinessValidator<C> businessValidator;
    protected final HandlerErrorMapper errorMapper;
    protected final java.util.function.Consumer<String> logger;
    protected final java.util.function.BiConsumer<String, Throwable> errLogger;

    protected AbstractCommandHandler(Builder<C, R> b) {
        this.options = Objects.requireNonNull(b.options);
        this.txManager = Objects.requireNonNull(b.txManager);
        this.eventPublisher = Objects.requireNonNull(b.eventPublisher);
        this.outbox = Objects.requireNonNull(b.outbox);
        this.authz = Objects.requireNonNull(b.authz);
        this.businessValidator = Objects.requireNonNull(b.businessValidator);
        this.errorMapper = Objects.requireNonNullElse(b.errorMapper, HandlerErrorMapper.defaultMapper());
        this.logger = Objects.requireNonNullElse(b.logger, msg -> {});
        this.errLogger = Objects.requireNonNullElse(b.errLogger, (m,t) -> {});
    }

    public static class Builder<C extends Command<R>, R> {
        private HandlerOptions options = HandlerOptions.defaultStrong();
        private TransactionManager txManager;
        private DomainEventPublisher eventPublisher;
        private OutboxService outbox;
        private AuthorizationService authz;
        private BusinessValidator<C> businessValidator = c -> {};
        private HandlerErrorMapper errorMapper;
        private java.util.function.Consumer<String> logger;
        private java.util.function.BiConsumer<String, Throwable> errLogger;

        public Builder<C,R> options(HandlerOptions v){ this.options=v; return this; }
        public Builder<C,R> txManager(TransactionManager v){ this.txManager=v; return this; }
        public Builder<C,R> eventPublisher(DomainEventPublisher v){ this.eventPublisher=v; return this; }
        public Builder<C,R> outbox(OutboxService v){ this.outbox=v; return this; }
        public Builder<C,R> authz(AuthorizationService v){ this.authz=v; return this; }
        public Builder<C,R> businessValidator(BusinessValidator<C> v){ this.businessValidator=v; return this; }
        public Builder<C,R> errorMapper(HandlerErrorMapper v){ this.errorMapper=v; return this; }
        public Builder<C,R> logger(java.util.function.Consumer<String> v){ this.logger=v; return this; }
        public Builder<C,R> errLogger(java.util.function.BiConsumer<String,Throwable> v){ this.errLogger=v; return this; }
    }

    @Override
    public final CompletionStage<R> handle(C command, CommandContext ctx) {
        // Autorização (opcional)
        if (options.enforceAuthorization()) {
            var perms = requiredPermissions(command);
            if (!perms.isEmpty()) {
                var uid = ctx.userId().orElse(null);
                if (uid == null || !authz.hasAll(uid, perms)) {
                    return CompletableFuture.failedFuture(new SecurityException("Permissões insuficientes: " + perms));
                }
            }
        }

        // Validações de negócio prévias (opcional)
        if (options.enforceBusinessValidation()) {
            try { businessValidator.validate(command); }
            catch (Exception ex) { return CompletableFuture.failedFuture(ex); }
        }

        // Retry (se configurado)
        if (options.retryMaxAttempts() > 1) {
            return executeWithRetry(command, ctx, options.retryMaxAttempts(),
                    options.retryInitialBackoff(), options.retryBackoffMultiplier(), options.retryJitter());
        }

        // Única tentativa
        return executeOnce(command, ctx);
    }

    private CompletionStage<R> executeOnce(C command, CommandContext ctx) {
        UnitOfWork uow = null;
        try {
            if (options.transactional()) {
                uow = txManager.newUnitOfWork();
                uow.begin();
            }

            // Core do domínio
            var result = doHandle(command, ctx);

            // Publicação de eventos de domínio (em memória/síncrono) — opcional
            if (options.publishDomainEvents()) {
                var events = collectDomainEventsToPublish(command, ctx);
                if (!events.isEmpty()) eventPublisher.publish(events);
            }

            // Outbox (CDC) — opcional
            if (options.useOutbox()) {
                var outboxMsgs = toOutboxMessages(command, ctx);
                for (var m : outboxMsgs) outbox.append(m.payload(), m.category(), m.key());
            }

            if (uow != null) uow.commit();
            return CompletableFuture.completedFuture(result);

        } catch (Throwable t) {
            if (uow != null) uow.rollback();
            var mapped = errorMapper.map(t);
            if (mapped.retryable() && options.retryMaxAttempts() > 1) {
                // Sinaliza para o mecanismo de retry externo (quando executeOnce é chamado via executeWithRetry)
            }
            errLogger.accept("Handler failure: " + mapped.code() + " - " + mapped.message(), t);
            return CompletableFuture.failedFuture(t);
        }
    }

    private CompletionStage<R> executeWithRetry(C command, CommandContext ctx, int attempts, Duration initial, double mult, boolean jitter) {
        CompletableFuture<R> out = new CompletableFuture<>();
        attempt(command, ctx, 1, attempts, initial.toMillis(), mult, jitter, out);
        return out;
    }

    private void attempt(C command, CommandContext ctx, int current, int max, long backoffMs, double mult, boolean jitter, CompletableFuture<R> out) {
        executeOnce(command, ctx).whenComplete((res, err) -> {
            if (err == null) { out.complete(res); return; }

            var mapped = errorMapper.map((Throwable) err);
            if (!mapped.retryable() || current >= max) {
                out.completeExceptionally(err); return;
            }
            long delay = (long) (backoffMs * Math.pow(mult, current-1));
            if (jitter) delay = (long)(delay * (0.5 + Math.random()));
            logger.accept("Retrying " + command.getClass().getSimpleName() + " attempt " + (current+1) + "/" + max + " in " + delay + "ms");
            Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> attempt(command, ctx, current+1, max, backoffMs, mult, jitter, out),
                        delay, java.util.concurrent.TimeUnit.MILLISECONDS);
        });
    }

    /** Core do domínio — você implementa. Deve ser side-effect free fora da UoW. */
    protected abstract R doHandle(C command, CommandContext ctx) throws Exception;

    /** Permissões necessárias (opcional). Pode ler anotação do Command. */
    protected Set<String> requiredPermissions(C command) { return Set.of(); }

    /** Coleta eventos de domínio pós-execução, se mantidos no Aggregate/Contexto. */
    protected List<Object> collectDomainEventsToPublish(C command, CommandContext ctx) { return List.of(); }

    /** Mensagens para Outbox (CDC) — tipicamente eventos de integração. */
    protected List<OutboxMessage> toOutboxMessages(C command, CommandContext ctx) { return List.of(); }

    /** Estrutura simples de mensagem para Outbox. */
    public record OutboxMessage(String category, String key, Object payload) {}
}
