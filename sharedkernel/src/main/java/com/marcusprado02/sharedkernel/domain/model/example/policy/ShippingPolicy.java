package com.marcusprado02.sharedkernel.domain.model.example.policy;

import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public interface ShippingPolicy {

    /**
     * Calcula o frete total de um carrinho/pedido.
     * @param totalItems quantidade total de itens (somatório das linhas)
     * @param subtotal   subtotal monetário (pode considerar descontos conforme sua regra)
     * @param ctx        contexto logístico (CEP/ZIP, modalidade, peso/volume se quiser estender)
     */
    Money shippingFor(int totalItems, Money subtotal, Context ctx);

    /** Contexto mínimo para frete. */
    record Context(String zip, boolean expedited, int totalWeightGrams, int totalVolumeCc) {}
}
