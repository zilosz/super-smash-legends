package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.PermanentPotion;
import com.github.zilosz.ssl.kit.Kit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.potion.PotionEffectType;

public class IceSkates extends PermanentPotion {

    public IceSkates(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public PotionEffectType getEffectType() {
        return PotionEffectType.SPEED;
    }
}
