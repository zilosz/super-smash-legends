package io.github.aura6.supersmashlegends.event;

import io.github.aura6.supersmashlegends.damage.Damage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

public class DamageEvent extends CustomEvent implements Cancellable {
    @Getter private final LivingEntity victim;
    @Getter @Setter private Damage damage;
    @Getter @Setter private boolean cancelled = false;

    public DamageEvent(LivingEntity victim, Damage damage) {
        this.victim = victim;
        this.damage = damage;
    }
}
