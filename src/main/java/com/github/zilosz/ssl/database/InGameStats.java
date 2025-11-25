package com.github.zilosz.ssl.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InGameStats {
  private int kills;
  private int deaths;
  private double damageDealt;
  private double damageTaken;

  public void addKills(int kills) {
    this.kills += kills;
  }

  public void addDeaths(int deaths) {
    this.deaths += deaths;
  }

  public void addDamageDealt(double damageDealt) {
    this.damageDealt += damageDealt;
  }

  public void addDamageTaken(double damageTaken) {
    this.damageTaken += damageTaken;
  }
}
