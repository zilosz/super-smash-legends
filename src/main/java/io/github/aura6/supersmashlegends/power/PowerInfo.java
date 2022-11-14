package io.github.aura6.supersmashlegends.power;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;

public class PowerInfo {
    @Getter private final Section config;

    public PowerInfo(Section config) {
        this.config = config;
    }

    public int getRarity() {
        return config.getInt("Rarity");
    }

    public String getName() {
        return config.getString("Name");
    }
}
