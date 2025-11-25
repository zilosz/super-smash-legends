package com.github.zilosz.ssl.database;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

@Getter
public class PlayerData {
  private final String uuid;
  private final InGameStats inGameStats;
  private final GameResultStats gameResultStats;
  @Setter private String name;
  @Setter private String kit;

  @BsonCreator
  public PlayerData(
      @BsonProperty("uuid")
      String uuid,
      @BsonProperty("name")
      String name,
      @BsonProperty(
          value = "inGameStats",
          useDiscriminator = true)
      InGameStats inGameStats,
      @BsonProperty(
          value = "gameResultStats",
          useDiscriminator = true)
      GameResultStats gameResultStats
  ) {
    this.uuid = uuid;
    this.name = name;
    this.inGameStats = inGameStats;
    this.gameResultStats = gameResultStats;
  }
}
