package com.github.zilosz.ssl.damage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attack {
    private final Damage damage;
    private final KnockBack kb;
    private int immunityTicks;

    public Attack(Damage damage, KnockBack kb, int immunityTicks) {
        this.damage = damage;
        this.kb = kb;
        this.immunityTicks = immunityTicks;
    }
}
