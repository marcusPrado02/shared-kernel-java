package com.marcusprado02.sharedkernel.domain.service.example.service;


import java.math.BigDecimal;
import java.util.*;

import com.marcusprado02.sharedkernel.domain.model.example.entity.LineAdjustment;
import com.marcusprado02.sharedkernel.domain.model.example.entity.PriceBreakdown;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;
import com.marcusprado02.sharedkernel.domain.service.DomainService;
import com.marcusprado02.sharedkernel.domain.service.error.DomainException;
import com.marcusprado02.sharedkernel.domain.service.error.ValidationException;
import com.marcusprado02.sharedkernel.domain.service.example.policy.DiscountPolicy;
import com.marcusprado02.sharedkernel.domain.service.example.policy.PromotionSpecification;
import com.marcusprado02.sharedkernel.domain.service.example.policy.ShippingPolicy;
import com.marcusprado02.sharedkernel.domain.service.example.policy.TaxPolicy;
import com.marcusprado02.sharedkernel.domain.service.result.DomainResult;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;


public final class PricingDomainService implements DomainService {

    private final List<DiscountPolicy> discountPolicies;
    private final TaxPolicy taxPolicy;
    private final ShippingPolicy shippingPolicy;
    private final List<PromotionSpecification> promoSpecs;
    private final ClockProvider clock;

    public record CartLine(String sku, int quantity, Money unitPrice, String category) {}

    public PricingDomainService(
            List<DiscountPolicy> discounts,
            TaxPolicy tax,
            ShippingPolicy shipping,
            List<PromotionSpecification> promoSpecs,
            ClockProvider clock
    ) {
        this.discountPolicies = List.copyOf(discounts);
        this.taxPolicy = Objects.requireNonNull(tax);
        this.shippingPolicy = Objects.requireNonNull(shipping);
        this.promoSpecs = List.copyOf(promoSpecs);
        this.clock = Objects.requireNonNull(clock);
    }

    public record Context(
            DiscountPolicy.Context discountCtx,
            TaxPolicy.Context taxCtx,
            ShippingPolicy.Context shippingCtx,
            PromotionSpecification.Context promoCtx
    ) {}

    /** Calcula preço total com políticas/specificações plugáveis — puro e determinístico. */
    public DomainResult<PriceBreakdown, DomainException> price(
            List<CartLine> lines,
            Context ctx
    ) {
        if (lines == null || lines.isEmpty()) {
            return DomainResult.err(new ValidationException("PRICING.EMPTY_CART", "Carrinho vazio"));
        }

        var adjustments = new ArrayList<LineAdjustment>();
        Money subtotal = null;
        Money totalDiscount = Money.of(BigDecimal.ZERO, lines.get(0).unitPrice().currency().getCurrencyCode());
        Money taxTotal = totalDiscount; // mesma moeda
        // 1) Subtotal
        for (var l : lines) {
            if (l.quantity() <= 0) {
                return DomainResult.err(new ValidationException("PRICING.INVALID_QTY", "Quantidade deve ser > 0"));
            }
            var lineTotal = l.unitPrice().times(BigDecimal.valueOf(l.quantity()));
            subtotal = (subtotal == null) ? lineTotal : subtotal.plus(lineTotal);
        }

        // 2) Discount(s) por política elegível
        for (var l : lines) {
            boolean eligible = promoSpecs.isEmpty() || promoSpecs.stream()
                    .anyMatch(s -> s.isEligible(
                            new PromotionSpecification.Subject("?", l.sku(), l.quantity(), l.category()),
                            ctx.promoCtx()
                    ));
            if (!eligible) continue;

            for (var dp : discountPolicies) {
                var d = dp.discountFor(l.sku(), l.quantity(), l.unitPrice(), ctx.discountCtx());
                if (d.amount().signum() > 0) {
                    totalDiscount = totalDiscount.plus(d);
                    adjustments.add(new LineAdjustment(l.sku(), "DISCOUNT", dp.getClass().getSimpleName(), d));
                }
            }
        }

        // 3) Taxas (sobre subtotal - descontos)
        var taxableBase = subtotal.minus(totalDiscount);
        for (var l : lines) {
            var lineBase = l.unitPrice().times(BigDecimal.valueOf(l.quantity()));
            var proportion = lineBase.amount().divide(subtotal.amount(), 8, java.math.RoundingMode.HALF_EVEN);
            var lineTaxBase = Money.of(taxableBase.amount().multiply(proportion), taxableBase.currency().getCurrencyCode());
            var tax = taxPolicy.taxFor(l.sku(), ((Money) lineTaxBase), ctx.taxCtx());
            if (((Money) tax).amount().signum() > 0) {
                taxTotal = taxTotal.plus((Money)tax);
                adjustments.add(new LineAdjustment(l.sku(), "TAX", taxPolicy.getClass().getSimpleName(), tax));
            }
        }

        // 4) Frete (política)
        var totalItems = lines.stream().mapToInt(CartLine::quantity).sum();
        var shipping = shippingPolicy.shippingFor(totalItems, taxableBase.plus(taxTotal), ctx.shippingCtx());

        // 5) Total final
        var grand = subtotal.minus(totalDiscount).plus(taxTotal).plus(shipping);

        var breakdown = new PriceBreakdown(subtotal, totalDiscount, taxTotal, shipping, grand, List.copyOf(adjustments));
        return DomainResult.ok(breakdown);
    }
}
