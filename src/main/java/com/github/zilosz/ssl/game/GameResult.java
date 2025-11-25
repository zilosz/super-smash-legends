package com.github.zilosz.ssl.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameResult {
  WIN("&a&lVictory!"), LOSE("&c&lLoss"), TIE("&e&lTie");

  private final String hologramString;
}
