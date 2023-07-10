package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PermanentPotion extends PassiveAbility {
    private PotionEffectType type;

    @Override
    public void activate() {
        super.activate();
        PotionEffect effect = YamlReader.getPotionEffect(this.config.getSection("Potion"));
        this.type = effect.getType();
        PotionEffectEvent.fromPotionEffect(this.player, effect).apply();
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.type != null) {
            this.player.removePotionEffect(this.type);
        }
    }

    @Override
    public String getUseType() {
        return "&oPassive";
    }
}
