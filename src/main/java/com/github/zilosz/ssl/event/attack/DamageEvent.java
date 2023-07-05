package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.damage.Damage;
import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

@Getter
public class DamageEvent extends CustomEvent implements Cancellable {
    private final LivingEntity victim;
    private final Damage damage;
    @Setter private boolean cancelled = false;
    @Setter private boolean isVoid;

    public DamageEvent(LivingEntity victim, Damage damage, boolean isVoid) {
        this.victim = victim;
        this.damage = damage;
        this.isVoid = isVoid;
    }

    public boolean willDie() {
        return this.getNewHealth() <= 0;
    }

    public double getNewHealth() {
        return this.victim.getHealth() - this.getFinalDamage();
    }

    public double getFinalDamage() {
        return this.damage.getFinalDamage(this.victim);
    }
}
