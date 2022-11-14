package io.github.aura6.supersmashlegends.game;

import lombok.Getter;
import lombok.Setter;

public class PlayerProfile {
    @Getter @Setter private int lives;
    @Getter @Setter private int deaths = 0;
    @Getter @Setter private int kills = 0;
    @Getter @Setter private int killStreak = 0;
    @Getter @Setter private double damageDealt = 0;
    @Getter @Setter private double damageTaken = 0;
    @Getter @Setter private int jewelsEarned;

    public PlayerProfile(int lives) {
        this.lives = lives;
    }

    public double getKd() {
        return (double) kills / deaths;
    }
}
