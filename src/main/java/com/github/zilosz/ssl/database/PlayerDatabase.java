package com.github.zilosz.ssl.database;

import com.github.zilosz.ssl.SSL;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PlayerDatabase {
    private final Map<Player, PlayerData> playerDataMap = new HashMap<>();
    private MongoCollection<PlayerData> mongoCollection;

    public void connect() {
        if (!this.getConfig().getBoolean("Enabled")) return;

        ClassModel<InGameStats> inGameStatsModel = ClassModel.builder(InGameStats.class)
                .enableDiscriminator(true)
                .build();

        ClassModel<GameResultStats> resultModel = ClassModel.builder(GameResultStats.class)
                .enableDiscriminator(true)
                .build();

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder()
                .automatic(true)
                .conventions(List.of(Conventions.ANNOTATION_CONVENTION))
                .register(inGameStatsModel, resultModel)
                .build()
        );

        CodecRegistry defaultRegistry = MongoClientSettings.getDefaultCodecRegistry();
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(defaultRegistry, pojoCodecRegistry);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(this.getConfig().getString("Uri")))
                .codecRegistry(codecRegistry)
                .build();

        MongoClient client = MongoClients.create(clientSettings);
        MongoDatabase database = client.getDatabase(this.getConfig().getString("Database"));

        String collectionName = this.getConfig().getString("Collection");

        if (!database.listCollectionNames().into(new HashSet<>()).contains(collectionName)) {
            database.createCollection(collectionName);
        }

        this.mongoCollection = database.getCollection(collectionName, PlayerData.class);
        this.mongoCollection.createIndex(Indexes.text("uuid"));
    }

    private Section getConfig() {
        return SSL.getInstance().getResources().getConfig().getSection("Database");
    }

    public void setupPlayerData(Player player) {
        PlayerData playerData;

        if (this.mongoCollection == null) {
            playerData = this.getBasicPlayerData(player);

        } else {
            FindIterable<PlayerData> iterable = this.mongoCollection.find(this.getUniquePlayerDataFilter(player));
            playerData = Optional.ofNullable(iterable.first()).orElse(this.getBasicPlayerData(player));
        }

        this.playerDataMap.put(player, playerData);
    }

    private PlayerData getBasicPlayerData(Player player) {
        String uuid = player.getUniqueId().toString();
        return new PlayerData(uuid, player.getName(), new InGameStats(), new GameResultStats());
    }

    private Bson getUniquePlayerDataFilter(Player player) {
        return Filters.eq("uuid", player.getUniqueId().toString());
    }

    public PlayerData getPlayerData(Player player) {
        return this.playerDataMap.get(player);
    }

    public void savePlayerData(Player player) {
        if (this.mongoCollection != null && this.getConfig().getBoolean("SaveData")) {
            ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
            PlayerData playerData = this.playerDataMap.get(player);
            this.mongoCollection.replaceOne(this.getUniquePlayerDataFilter(player), playerData, replaceOptions);
        }
    }

    public void removePlayerData(Player player) {
        this.playerDataMap.remove(player);
    }

    public Stream<PlayerData> findAllPlayerData() {
        if (this.mongoCollection == null) return Stream.empty();
        return StreamSupport.stream(this.mongoCollection.find().spliterator(), true);
    }
}
