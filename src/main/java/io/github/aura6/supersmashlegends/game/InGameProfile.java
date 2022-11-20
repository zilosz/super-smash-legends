package io.github.aura6.supersmashlegends.game;

import lombok.Getter;
import lombok.Setter;

public class InGameProfile {
    @Getter @Setter private int lives;
    @Getter @Setter private int deaths = 0;
    @Getter @Setter private int kills = 0;
    @Getter @Setter private int killStreak = 0;
    @Getter @Setter private double damageDealt = 0;
    @Getter @Setter private double damageTaken = 0;
    @Getter @Setter private int jewelsEarned = 0;
    @Getter @Setter private boolean winner = false;

    public InGameProfile(int lives) {
        this.lives = lives;
    }
}
