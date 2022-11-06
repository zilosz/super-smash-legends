package io.github.aura6.supersmashlegends.damage;

import lombok.Getter;

public enum DamageType {
    MELEE("&cMelee");

    @Getter private final String name;

    DamageType(String name) {
        this.name = name;
    }
}
