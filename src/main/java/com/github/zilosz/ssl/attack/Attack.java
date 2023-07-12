package com.github.zilosz.ssl.attack;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Attack {
    @Setter private String name;
    private final Damage damage;
    private final KnockBack kb;
    @Setter private int immunityTicks;

    public Attack(String name, Damage damage, KnockBack kb, int immunityTicks) {
        this.name = name;
        this.damage = damage;
        this.kb = kb;
        this.immunityTicks = immunityTicks;
    }
}
