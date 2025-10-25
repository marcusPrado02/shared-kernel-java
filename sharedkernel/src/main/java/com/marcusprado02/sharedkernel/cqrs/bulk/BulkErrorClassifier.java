package com.marcusprado02.sharedkernel.cqrs.bulk;

/** Classifica erros em rejeição (não-retriável) x falha (potencialmente retriável). */
public interface BulkErrorClassifier {
    record Classification(boolean reject) {}
    Classification map(Throwable t);

    static BulkErrorClassifier defaultClassifier() {
        return t -> {
            var name = t.getClass().getSimpleName().toLowerCase();
            boolean reject = name.contains("validation") || name.contains("illegalargument") || name.contains("security");
            return new Classification(reject);
        };
    }
}
