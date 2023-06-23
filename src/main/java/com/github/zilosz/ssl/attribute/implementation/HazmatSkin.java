package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.PermanentPotion;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class HazmatSkin extends PermanentPotion {
    private final Set<PotionEffectType> resistedEffects = new HashSet<>();

    @Override
    public void activate() {
        super.activate();

        for (String effect : this.config.getStringList("ResistedPotionEffects")) {
            this.resistedEffects.add(PotionEffectType.getByName(effect));
        }
    }

    @Override
    public PotionEffectType getEffectType() {
        return PotionEffectType.FIRE_RESISTANCE;
    }

    @Override
    public String getUseType() {
        return "&oSimply &oExist";
    }

    @EventHandler
    public void onPotionEffect(PotionEffectEvent event) {
        if (event.getEntity() == this.player && this.resistedEffects.contains(event.getType())) {
            event.setCancelled(true);
        }
    }
}
