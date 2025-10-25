package com.marcusprado02.sharedkernel.adapters.in.rest.pagination.mongo;

import com.marcusprado02.sharedkernel.adapters.in.rest.pagination.*;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

public final class MongoPaginationAdapter implements PaginationAdapter<Document, Document> {
    private final MongoCollection<Document> coll;
    private final CursorCodec codec;

    public MongoPaginationAdapter(MongoCollection<Document> coll, CursorCodec codec){
        this.coll = coll; this.codec = codec;
    }

    @Override public PageResult<Document> page(Document baseFilter, PageRequestSpec spec) {
        return switch (spec.mode()){
            case OFFSET -> pageOffset(baseFilter, spec);
            case CURSOR -> pageKeyset(baseFilter, spec);
        };
    }

    private PageResult<Document> pageOffset(Document base, PageRequestSpec pr){
        var sortDoc = toSort(pr.sort());
        var cur = coll.find(base).sort(sortDoc).skip(pr.offset()).limit(pr.limit()+1);
        var raw = cur.into(new ArrayList<>());
        boolean hasMore = raw.size() > pr.limit();
        var items = hasMore ? raw.subList(0, pr.limit()) : raw;
        String next = null;
        if (hasMore && !items.isEmpty()){
            var last = items.get(items.size()-1);
            next = codec.encode(codec.makeFromRow(extractKeyValues(last, pr.sort()), Direction.ASC));
        }
        Long approx = null; // em Mongo, count pode ser caro; use $collStats se quiser aproximar
        return new PageResult<>(items, hasMore, null, approx, next, null);
    }

    private PageResult<Document> pageKeyset(Document base, PageRequestSpec pr){
        var after = codec.decode(pr.after());
        var before = codec.decode(pr.before());
        var dir = (before != null) ? Direction.DESC : Direction.ASC;
        var payload = (before!=null) ? before : after;

        var sortDoc = toSort(pr.sort(), before!=null);
        var filter = (payload==null || payload.keyValues().isEmpty()) ? base
                : and(base, keysetFilter(payload, pr.sort(), dir));
        var raw = coll.find(filter).sort(sortDoc).limit(pr.limit()+1).into(new ArrayList<>());
        var items = (before!=null) ? reverseAndTrim(raw, pr.limit()) : trim(raw, pr.limit());
        boolean hasMore = raw.size() > pr.limit();

        String next = null, prev = null;
        if (!items.isEmpty()){
            var first = items.get(0);
            var last = items.get(items.size()-1);
            next = hasMore ? codec.encode(codec.makeFromRow(extractKeyValues(last, pr.sort()), Direction.ASC)) : null;
            prev = codec.encode(codec.makeFromRow(extractKeyValues(first, pr.sort()), Direction.DESC));
        }
        return new PageResult<>(items, hasMore, null, null, next, prev);
    }

    private static org.bson.conversions.Bson keysetFilter(CursorPayload p, List<Order> sort, Direction dir){
        // Para dois campos (a,b): (a,b) > (va,vb) => a>va OR (a=va AND b>vb)
        // Generalize recursivamente:
        return lexicographicGt(sort, p.keyValues(), dir==Direction.DESC);
    }
    private static org.bson.conversions.Bson lexicographicGt(List<Order> sort, Map<String,Object> kv, boolean reversed){
        // reversed significa inverter operadores (> vira <)
        var ops = new ArrayList<org.bson.conversions.Bson>();
        for (int i=0; i<sort.size(); i++){
            var leadingEquals = new ArrayList<org.bson.conversions.Bson>();
            for (int j=0; j<i; j++){
                leadingEquals.add(eq(sort.get(j).field(), kv.get(sort.get(j).field())));
            }
            var field = sort.get(i).field();
            var cmp = reversed ? lt(field, kv.get(field)) : gt(field, kv.get(field));
            var conj = leadingEquals.isEmpty() ? cmp : and(leadingEquals).toBsonDocument(Document.class, com.mongodb.MongoClientSettings.getDefaultCodecRegistry()).toBsonDocument();
            ops.add(and(leadingEquals.isEmpty()? new org.bson.Document() : and(leadingEquals), cmp));
        }
        return or(ops);
    }

    private static Map<String,Object> extractKeyValues(Document d, List<Order> sort){
        Map<String,Object> out = new LinkedHashMap<>();
        for (var o : sort) out.put(o.field(), d.get(o.field()));
        return out;
    }

    private static Document toSort(List<Order> sort){ return toSort(sort, false); }
    private static Document toSort(List<Order> sort, boolean reversed){
        Document s = new Document();
        for (var o : sort){
            int dir = o.direction()==Direction.ASC ? 1 : -1;
            if (reversed) dir = -dir;
            s.append(o.field(), dir);
        }
        return s;
    }
    private static <T> List<T> trim(List<T> list, int limit){ return list.size()>limit? list.subList(0, limit):list; }
    private static <T> List<T> reverseAndTrim(List<T> list, int limit){ Collections.reverse(list); return trim(list, limit); }
}
