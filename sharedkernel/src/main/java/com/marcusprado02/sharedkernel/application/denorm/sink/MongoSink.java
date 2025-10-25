package com.marcusprado02.sharedkernel.application.denorm.sink;
import java.util.Map;

import com.marcusprado02.sharedkernel.application.denorm.DenormSink;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

public class MongoSink implements DenormSink {
    private final MongoCollection<Document> col;

    public MongoSink(MongoCollection<Document> col) { this.col = col; }

    @Override public void upsert(String collection, String id, Map<String, Object> doc) {
        col.replaceOne(new Document("_id", id),
                       new Document(doc).append("_id", id),
                       new ReplaceOptions().upsert(true));
    }
    @Override public void delete(String collection, String id) {
        col.deleteOne(new Document("_id", id));
    }
}