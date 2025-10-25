package com.marcusprado02.sharedkernel.application.denorm.example.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.marcusprado02.sharedkernel.application.denorm.*;
import com.marcusprado02.sharedkernel.application.denorm.sink.*;
import com.marcusprado02.sharedkernel.application.denorm.store.*;
import com.marcusprado02.sharedkernel.application.port.example.impl.CustomerPort;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

@Component
public class OrderDenormalizer extends BaseDenormalizer {

    private final DenormSink sql;
    private final DenormSink search;
    private final List<DenormPlan> plans;

    public OrderDenormalizer(OffsetStore offsets, DeadLetterStore dlq,
                             SqlSink sql, OpenSearchSink search,
                             CustomerPort customerPort) {
        super(offsets, dlq);
        this.sql = sql; this.search = search;

        Enricher customerNameEnricher = doc -> {
            var customerId = String.valueOf(doc.get("customer_id"));
            var name = customerPort.findName(customerId).orElse("UNKNOWN");
            doc.put("customer_name", name);
            return doc;
        };

        this.plans = List.of(
            // OrderCreated / StatusChanged / ItemAdded → tabela SQL "order_flat"
            DenormPlan.to("order_flat")
                .whenType("OrderCreated","OrderStatusChanged","OrderItemAdded","OrderDeleted")
                .map(e -> OrderMappers.toFlatDoc(e))
                .enrich(customerNameEnricher)
                .build(),
            // Mesmas mudanças → índice "orders_search"
            DenormPlan.to("orders_search")
                .whenType("OrderCreated","OrderStatusChanged","OrderItemAdded")
                .map(e -> OrderMappers.toSearchDoc(e))
                .enrich(customerNameEnricher)
                .build()
        );
    }

    @Override public String name() { return "order-denorm-v1"; }

    @Override protected void route(EventEnvelope env) {
        for (var p : plans) if (p.matches(env)) {
            var isDelete = env.metadata().eventType().toString().equals("OrderDeleted");
            if (isDelete) { sql.delete("order_flat", env.metadata().aggregateId()); search.delete("orders_search", env.metadata().aggregateId()); }
            else {
                p.execute(p == plans.get(0) ? sql : search, env);
            }
        }
    }
}
