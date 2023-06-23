package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.PermanentPotion;
import org.bukkit.potion.PotionEffectType;

public class IceSkates extends PermanentPotion {

    @Override
    public PotionEffectType getEffectType() {
        return PotionEffectType.SPEED;
    }
}
