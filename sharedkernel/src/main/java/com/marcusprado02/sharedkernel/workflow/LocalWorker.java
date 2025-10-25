package com.marcusprado02.sharedkernel.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.workflow.model.WorkflowTask;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public final class LocalWorker implements Runnable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WorkflowStore store;
    private final String queue;
    // ⬇️ NÃO engesse o tipo; aceite qualquer Activity genérica
    private final Map<String, Activity<?, ?>> activities;

    public LocalWorker(WorkflowStore store, String queue, Map<String, Activity<?, ?>> activities) {
        this.store = Objects.requireNonNull(store, "store");
        this.queue = Objects.requireNonNull(queue, "queue");
        this.activities = Objects.requireNonNull(activities, "activities");
    }

    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            var now = OffsetDateTime.now();
            store.pollTask(queue, now).ifPresent(task -> {
                if (!store.claimTask(task.taskId())) return;
                execute(task);
            });
            sleepQuietly(50);
        }
    }

    private void execute(WorkflowTask task) {
        try {
            var a = activities.get(task.activityName());
            if (a == null) {
                store.appendHistory(task.workflowId(), "ActivityMissing",
                        Map.of("activity", task.activityName(), "taskId", task.taskId()));
                // Sem métodos formais de conclusão/reagendamento no WorkflowStore:
                // apenas registramos—se você adotar complete/fail/reschedule, chame-os aqui.
                return;
            }

            // Parse do input como Object (o Activity<I,O> sabe o que esperar)
            Object input = null;
            var json = task.inputJson();
            if (json != null && !json.isBlank()) {
                input = MAPPER.readValue(json, Object.class);
            }

            // Idempotência: use a idemKey da task se já vier preenchida;
            // se não, deixa a activity gerar (quando fizer sentido).
            String idemKey = task.idemKey();
            if (idemKey == null || idemKey.isBlank()) {
                idemKey = idempotencyKeyPolymorphic(a, input, new DefaultActivityContext(task));
            }

            if (idemKey != null && store.seenIdempotency(task.activityName(), idemKey)) {
                store.appendHistory(task.workflowId(), "ActivityIdempotentSkip",
                        Map.of("activity", task.activityName(), "taskId", task.taskId(), "idemKey", idemKey));
                return;
            }

            // Execução polimórfica (sem “amarrar” generics aqui)
            Object out = executePolymorphic(a, input, new DefaultActivityContext(task));

            // Se você estender WorkflowStore, grave a idempotência e complete a task:
            // store.recordIdempotency(task.activityName(), idemKey);
            // store.completeTask(task.taskId(), writeJson(out));

            store.appendHistory(task.workflowId(), "ActivityCompleted",
                    Map.of("activity", task.activityName(), "taskId", task.taskId()));

        } catch (Exception e) {
            // Se você estender WorkflowStore, faça retry/backoff aqui. Por ora, só histórico:
            store.appendHistory(task.workflowId(), "ActivityError",
                    Map.of("activity", task.activityName(), "taskId", task.taskId(), "message", e.getMessage()));
        }
    }

    // ===== Helpers genéricos (ponto único de @SuppressWarnings) =====

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object executePolymorphic(Activity<?, ?> act, Object input, ActivityContext ctx) throws Exception {
        // Erasure: chamamos como raw type e deixamos o próprio Activity validar/converter o input
        return ((Activity) act).execute(ctx, input);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static String idempotencyKeyPolymorphic(Activity<?, ?> act, Object input, ActivityContext ctx) {
        return ((Activity) act).idempotencyKey(ctx, input);
    }

    private static String writeJson(Object obj) {
        if (obj == null) return null;
        try { return MAPPER.writeValueAsString(obj); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Failed to serialize output", e); }
    }

    private static void sleepQuietly(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
