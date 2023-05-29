package io.github.aura6.supersmashlegends.game;

import lombok.Getter;

public enum GameResult {
    WIN("&a&lVictory!", "wins"),
    LOSE("&c&lLoss", "losses"),
    TIE("&e&lTie", "ties");

    @Getter private final String hologramString;
    @Getter private final String dbString;

    GameResult(String hologramString, String dbString) {
        this.hologramString = hologramString;
        this.dbString = dbString;
    }
}
