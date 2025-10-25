package com.marcusprado02.sharedkernel.readmodel.example.impl;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "order_summary_view",
    indexes = {
        @Index(name="idx_osv_tenant_status_created", columnList="tenant_id,status,created_at DESC"),
        @Index(name="idx_osv_customer_name", columnList="customer_name")
})
public class OrderSummaryView {
    @Id
    private Long orderId;

    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private String status;
    @Column(nullable=false) private String customerName;
    @Column(nullable=false) private Integer itemCount;
    @Column(nullable=false) private java.math.BigDecimal totalValue;
    @Column(nullable=false, name="created_at") private OffsetDateTime createdAt;
    @Column(name="last_status_at") private OffsetDateTime lastStatusAt;
    @Column(nullable=false) private boolean deleted;

    // getters/setters
}
