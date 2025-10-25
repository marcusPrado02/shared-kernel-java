package com.marcusprado02.sharedkernel.adapters.in.rest.pagination;

import java.util.ArrayList;
import java.util.List;

/** Especificação de página pedida pelo cliente. */
public record PageRequestSpec(
        PageMode mode,
        Integer offset,        // usado quando mode = OFFSET
        Integer limit,         // limite sempre respeitado, p.ex. 1..1000
        String after,          // cursor opcional (depois)
        String before,         // cursor opcional (antes)
        List<Order> sort       // ordenação determinística, sempre inclui tie-breaker
) {
    public static Builder builder(){ return new Builder(); }
    public static final class Builder {
        private PageMode mode = PageMode.CURSOR;
        private Integer offset = 0;
        private Integer limit = 20;
        private String after;
        private String before;
        private final List<Order> sort = new ArrayList<>();
        public Builder mode(PageMode m){ this.mode = m; return this; }
        public Builder offset(Integer o){ this.offset = o; return this; }
        public Builder limit(Integer l){ this.limit = l; return this; }
        public Builder after(String a){ this.after = a; return this; }
        public Builder before(String b){ this.before = b; return this; }
        public Builder addOrder(String field, Direction dir, Order.Nulls n){ this.sort.add(new Order(field, dir, n)); return this; }
        public PageRequestSpec build(){ return new PageRequestSpec(mode, offset, limit, after, before, List.copyOf(sort)); }
    }
}
