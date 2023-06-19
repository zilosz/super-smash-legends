package com.github.zilosz.ssl.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class PermanentPotion extends PassiveAbility {

    public PermanentPotion(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    public abstract PotionEffectType getEffectType();

    @Override
    public String getUseType() {
        return "Potion Effect";
    }

    @Override
    public void activate() {
        super.activate();
        player.addPotionEffect(new PotionEffect(getEffectType(), Integer.MAX_VALUE, config.getInt("Amplifier")));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        player.removePotionEffect(getEffectType());
    }
}
