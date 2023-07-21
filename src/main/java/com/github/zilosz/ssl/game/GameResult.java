package com.github.zilosz.ssl.game;

import lombok.Getter;

@Getter
public enum GameResult {
    WIN("&a&lVictory!"),
    LOSE("&c&lLoss"),
    TIE("&e&lTie");

    private final String hologramString;

    GameResult(String hologramString) {
        this.hologramString = hologramString;
    }
}
