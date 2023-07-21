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
}
