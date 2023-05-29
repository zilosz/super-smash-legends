package io.github.aura6.supersmashlegends.game;

import io.github.aura6.supersmashlegends.kit.Kit;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InGameProfile {
    private int lives;
    private int deaths = 0;
    private int kills = 0;
    private double damageDealt = 0;
    private double damageTaken = 0;
    private Kit kit;
    private GameResult gameResult;

    public InGameProfile(int lives, Kit kit) {
        this.lives = lives;
        this.kit = kit;
    }
}
