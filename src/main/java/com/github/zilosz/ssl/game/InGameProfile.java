package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.database.GameResultStats;
import com.github.zilosz.ssl.database.InGameStats;
import com.github.zilosz.ssl.database.PlayerData;
import com.github.zilosz.ssl.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter
public class InGameProfile {
  private final InGameStats stats = new InGameStats();
  @Setter private Kit kit;
  @Setter private int lives;
  @Setter private GameResult gameResult;

  public InGameProfile(Kit kit, int lives) {
    this.kit = kit;
    this.lives = lives;
  }

  public void takeLife() {
    lives--;
  }

  public void updatePlayerData(PlayerData playerData) {
    InGameStats totalStats = playerData.getInGameStats();
    totalStats.addKills(stats.getKills());
    totalStats.addDeaths(stats.getDeaths());
    totalStats.addDamageDealt(stats.getDamageDealt());
    totalStats.addDamageTaken(stats.getDamageTaken());

    GameResultStats gameResultStats = playerData.getGameResultStats();

    switch (gameResult) {

      case WIN:
        gameResultStats.addWin();
        break;

      case TIE:
        gameResultStats.addTie();
        break;

      case LOSE:
        gameResultStats.addLoss();
    }
  }
}
