package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attack.Damage;
import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

@Getter
public abstract class AbstractDamageEvent extends CustomEvent implements Cancellable {
    private final LivingEntity victim;
    @Setter private boolean cancelled = false;

    public AbstractDamageEvent(LivingEntity victim) {
        this.victim = victim;
    }

    public boolean willDie() {
        return this.getNewHealth() <= 0;
    }

    public double getNewHealth() {
        return this.victim.getHealth() - this.getFinalDamage();
    }

    public double getFinalDamage() {
        return this.getDamage().getFinalDamage(this.victim);
    }

    public abstract Damage getDamage();
}
