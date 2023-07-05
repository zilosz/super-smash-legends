package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

public class PermanentPotion extends PassiveAbility {
    private PotionEffectType type;

    @Override
    public void activate() {
        super.activate();
        int amplifier = this.config.getOptionalInt("Amplifier").orElse(1);
        this.type = PotionEffectType.getByName(this.config.getString("Type"));
        new PotionEffectEvent(this.player, this.type, 1_000_000, amplifier).apply();
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
