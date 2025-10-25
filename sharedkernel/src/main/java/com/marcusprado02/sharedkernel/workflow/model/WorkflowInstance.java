package com.marcusprado02.sharedkernel.workflow.model;

import com.marcusprado02.sharedkernel.cqrs.command.CommandResult.Status;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity @Table(name="wf_instances")
public class WorkflowInstance<T> {
    @Id public String id;
    public String name;
    @Enumerated(EnumType.STRING) public Status status; // RUNNING, COMPLETED, FAILED, CANCELED
    public Integer version;
    public String inputJson;
    public String outputJson;
    public java.time.OffsetDateTime updatedAt;

    // Fábrica
    public static <I> WorkflowInstance newInstance(String id, String name, I input) {
        var inst = new WorkflowInstance();
        inst.id = id;
        inst.name = name;
        inst.status = Status.ACCEPTED;
        inst.version = 1;
        inst.inputJson = serializeToJson(input);
        inst.updatedAt = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
        return inst;
    }

    private static String serializeToJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize input to JSON", e);
        }
    }

     // ✅ usado pelo Engine; você pode delegar a uma state machine interna
    public void onSignal(String signalName, Object payload) {
        // processa o sinal (ex: avança o grafo, atualiza estado, etc)
        this.updatedAt = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
        // lógica para processar o sinal
        switch (signalName) {
            case "NextStep":
                // avança para o próximo passo
                break;
            case "Cancel":
                // cancela a instância
                break;
            default:
                throw new IllegalArgumentException("Unknown signal: " + signalName);
        }
    }
}