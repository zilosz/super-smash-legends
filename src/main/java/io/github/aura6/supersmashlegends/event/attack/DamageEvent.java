package io.github.aura6.supersmashlegends.event.attack;

import io.github.aura6.supersmashlegends.damage.DamageSettings;
import io.github.aura6.supersmashlegends.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

@Getter
public class DamageEvent extends CustomEvent implements Cancellable {
    @Setter private boolean cancelled = false;
    private final LivingEntity victim;
    private final DamageSettings damageSettings;
    @Setter private boolean isVoid;

    public DamageEvent(LivingEntity victim, DamageSettings damageSettings, boolean isVoid) {
        this.victim = victim;
        this.damageSettings = damageSettings;
        this.isVoid = isVoid;
    }

    public double getFinalDamage() {
        return this.damageSettings.getFinalDamage(this.victim);
    }

    public double getNewHealth() {
        return this.victim.getHealth() - this.getFinalDamage();
    }

    public boolean willDie() {
        return this.getNewHealth() <= 0;
    }
}
