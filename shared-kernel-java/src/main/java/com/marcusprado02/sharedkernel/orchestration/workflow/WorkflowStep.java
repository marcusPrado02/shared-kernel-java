package com.marcusprado02.sharedkernel.orchestration.workflow;


/**
 * Um passo de workflow recebe e atualiza o contexto. Pode lançar WorkflowException em caso de
 * falha.
 */
public interface WorkflowStep<C extends WorkflowContext> {

    /** Executa o step, alterando o contexto conforme necessário. */
    void execute(C context) throws WorkflowException;

    /**
     * Em caso de falha, passo pode implementar compensação revertendo alterações feitas no contexto
     * ou em recursos externos.
     */
    default void compensate(C context) { /* noop por padrão */ }
}
