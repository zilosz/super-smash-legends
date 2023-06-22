package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.kit.Kit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.potion.PotionEffectType;

public abstract class PermanentPotion extends PassiveAbility {

    public PermanentPotion(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void activate() {
        super.activate();
        int amplifier = this.config.getInt("Amplifier");
        new PotionEffectEvent(this.player, this.getEffectType(), 10_000, amplifier).apply();
    }

    public abstract PotionEffectType getEffectType();

    @Override
    public void deactivate() {
        super.deactivate();
        this.player.removePotionEffect(this.getEffectType());
    }

    @Override
    public String getUseType() {
        return "&oSimply &oExist";
    }
}
