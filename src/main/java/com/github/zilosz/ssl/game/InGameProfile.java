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

    public void updatePlayerData(PlayerData playerData) {
        InGameStats totalInGameStats = playerData.getInGameStats();
        totalInGameStats.setKills(totalInGameStats.getKills() + this.stats.getKills());
        totalInGameStats.setDeaths(totalInGameStats.getDeaths() + this.stats.getDeaths());
        totalInGameStats.setDamageDealt(totalInGameStats.getDamageDealt() + this.stats.getDamageDealt());
        totalInGameStats.setDamageTaken(totalInGameStats.getDamageTaken() + this.stats.getDamageTaken());

        GameResultStats gameResultStats = playerData.getGameResultStats();

        switch (this.gameResult) {

            case WIN:
                gameResultStats.setWins(gameResultStats.getWins() + 1);
                break;

            case TIE:
                gameResultStats.setTies(gameResultStats.getTies() + 1);
                break;

            case LOSE:
                gameResultStats.setLosses(gameResultStats.getLosses() + 1);
        }
    }
}
