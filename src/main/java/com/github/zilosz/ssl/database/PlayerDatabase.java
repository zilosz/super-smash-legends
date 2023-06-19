package com.github.zilosz.ssl.database;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerDatabase {
    private MongoCollection<Document> mongoCollection;

    public void init(String uri, String db, String collection) {
        this.mongoCollection = MongoClients.create(uri).getDatabase(db).getCollection(collection);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(UUID uuid, String key, T ifNull, T ifDisabled) {
        if (this.mongoCollection == null) return ifDisabled;

        CompletableFuture<T> future = new CompletableFuture<>();
        Document doc = this.mongoCollection.find(new Document("uuid", uuid.toString())).first();
        future.complete(doc == null || doc.get(key) == null ? null : (T) doc.get(key));

        try {
            T result = future.get();
            return result == null ? ifNull : result;

        } catch (InterruptedException | ExecutionException e) {
            return ifNull;
        }
    }

    public <T> void set(UUID uuid, String key, T value, Bson update) {
        if (this.mongoCollection == null) return;

        Document document = new Document("uuid", uuid.toString());
        Document existing = this.mongoCollection.find(document).first();

        if (existing == null) {
            document.put(key, value);
            this.mongoCollection.insertOne(document);

        } else {
            this.mongoCollection.updateOne(existing, update);
        }
    }

    public <T> void set(UUID uuid, String key, T value) {
        this.set(uuid, key, value, Updates.set(key, value));
    }

    public void increment(UUID uuid, String key, Number value) {
        this.set(uuid, key, value, Updates.inc(key, value));
    }

    public List<Document> getDocuments() {
        if (this.mongoCollection == null) return Collections.emptyList();
        return Lists.newArrayList(this.mongoCollection.find().iterator());
    }
}
