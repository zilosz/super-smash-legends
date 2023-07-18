package com.github.zilosz.ssl.database;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerDatabase {
    private MongoCollection<Document> mongoCollection;

    public void init(String uri, String db, String collection) {
        this.mongoCollection = MongoClients.create(uri).getDatabase(db).getCollection(collection);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(UUID uuid, String key, T ifNull, T ifDisabled) {
        return this.mongoCollection == null ? ifDisabled : (T) this.getUserDocument(uuid).getOrDefault(key, ifNull);
    }

    private Document getUserDocument(UUID uuid) {
        Document document = new Document("uuid", uuid.toString());

        return Optional.ofNullable(this.mongoCollection.find(document).first()).orElseGet(() -> {
            this.mongoCollection.insertOne(document);
            return document;
        });
    }

    public <T> void set(UUID uuid, String key, T value) {
        this.set(uuid, key, value, Updates.set(key, value));
    }

    public <T> void set(UUID uuid, String key, T value, Bson update) {
        if (this.mongoCollection != null) {
            Document document = this.getUserDocument(uuid);
            document.put(key, value);
            this.mongoCollection.updateOne(document, update);
        }
    }

    public void increment(UUID uuid, String key, Number value) {
        this.set(uuid, key, value, Updates.inc(key, value));
    }

    public List<Document> getDocuments() {
        if (this.mongoCollection == null) return Collections.emptyList();
        return Lists.newArrayList(this.mongoCollection.find().iterator());
    }
}
