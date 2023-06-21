package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class PermanentPotion extends PassiveAbility {

    public PermanentPotion(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void activate() {
        super.activate();
        int amplifier = this.config.getInt("Amplifier");
        this.player.addPotionEffect(new PotionEffect(this.getEffectType(), Integer.MAX_VALUE, amplifier));
    }

    public abstract PotionEffectType getEffectType();

    @Override
    public void deactivate() {
        super.deactivate();
        this.player.removePotionEffect(this.getEffectType());
    }

    @Override
    public String getUseType() {
        return "Potion Effect";
    }
}