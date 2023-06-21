package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.PermanentPotion;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.kit.Kit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class HazmatSkin extends PermanentPotion {
    private final Set<PotionEffectType> resistedEffects = new HashSet<>();

    public HazmatSkin(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

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
        return "Potion-Effect/Passive";
    }

    @EventHandler
    public void onPotionEffect(PotionEffectEvent event) {
        if (event.getEntity() == this.player && this.resistedEffects.contains(event.getType())) {
            event.setCancelled(true);
        }
    }
}
