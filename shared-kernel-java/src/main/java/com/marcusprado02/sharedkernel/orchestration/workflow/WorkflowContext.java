package com.marcusprado02.sharedkernel.orchestration.workflow;


import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Contexto genérico de um workflow.
 */
@Getter
@Setter
public class WorkflowContext {

    /** Identificador único da instância de workflow */
    private final UUID workflowId = UUID.randomUUID();

    /** Versão para controle de concorrência e persistência */
    private long version = 0;

    /** Variáveis de dados compartilhadas entre steps */
    private final Map<String, Object> variables;

    /** Marca o workflow como concluído */
    private boolean completed = false;

    public WorkflowContext(Map<String, Object> variables) {
        this.variables = variables;
    }

    /** Incrementa versão sempre que o estado mudar */
    public void bumpVersion() {
        this.version++;
    }
}

