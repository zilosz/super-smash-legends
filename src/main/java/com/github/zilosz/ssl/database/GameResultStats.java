package com.github.zilosz.ssl.database;

import lombok.Getter;

@Getter
public class GameResultStats {
  private int wins;
  private int losses;
  private int ties;

  public void addWin() {
    wins++;
  }

  public void addLoss() {
    losses++;
  }

  public void addTie() {
    ties++;
  }
}
