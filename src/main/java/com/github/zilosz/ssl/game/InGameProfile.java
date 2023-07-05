package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InGameProfile {
    private int lives;
    private int deaths;
    private int kills;
    private double damageDealt;
    private double damageTaken;
    private Kit kit;
    private GameResult gameResult;

    public InGameProfile(int lives, Kit kit) {
        this.lives = lives;
        this.kit = kit;
    }
}
