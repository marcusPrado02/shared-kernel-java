package com.marcusprado02.sharedkernel.orchestration.workflow;



import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra execução de uma sequência de WorkflowStep<C>.
 */
@Component
public class WorkflowOrchestrator<C extends WorkflowContext> {

    private final List<WorkflowStep<C>> steps;
    private final WorkflowContextRepository<C> repo; // persiste estado do contexto

    public WorkflowOrchestrator(List<WorkflowStep<C>> steps, WorkflowContextRepository<C> repo) {
        this.steps = steps;
        this.repo = repo;
    }

    /**
     * Inicia o workflow: persiste contexto e executa todos os steps em ordem.
     */
    @Transactional
    public void start(C context) {
        repo.save(context);
        try {
            runSteps(context);
            context.setCompleted(true);
            repo.save(context);
        } catch (WorkflowException ex) {
            repo.save(context);
            throw ex;
        }
    }

    private void runSteps(C context) {
        for (WorkflowStep<C> step : steps) {
            try {
                step.execute(context);
                context.bumpVersion();
                repo.save(context);
            } catch (Exception ex) {
                // executa compensação dos steps já executados
                compensateUpTo(context, step);
                throw new WorkflowException("Falha em step " + step.getClass().getSimpleName(), ex);
            }
        }
    }

    private void compensateUpTo(C context, WorkflowStep<C> failedStep) {
        int idx = steps.indexOf(failedStep);
        for (int i = idx; i >= 0; i--) {
            steps.get(i).compensate(context);
        }
    }
}
