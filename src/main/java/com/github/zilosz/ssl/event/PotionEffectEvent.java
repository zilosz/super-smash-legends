package com.github.zilosz.ssl.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
@Setter
public class PotionEffectEvent extends CustomEvent implements Cancellable {
    private final LivingEntity entity;
    private boolean cancelled = false;
    private PotionEffectType type;
    private int duration;
    private int amplifier;

    public PotionEffectEvent(LivingEntity entity, PotionEffectType type, int duration, int amplifier) {
        this.entity = entity;
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public static PotionEffectEvent fromPotionEffect(LivingEntity entity, PotionEffect effect) {
        return new PotionEffectEvent(entity, effect.getType(), effect.getDuration(), effect.getAmplifier());
    }

    public void apply() {
        Bukkit.getPluginManager().callEvent(this);

        if (!this.cancelled) {
            this.entity.addPotionEffect(new PotionEffect(this.type, this.duration, this.amplifier));
        }
    }
}
