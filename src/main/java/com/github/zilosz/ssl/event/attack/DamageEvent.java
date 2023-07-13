package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attack.AttackSource;
import com.github.zilosz.ssl.attack.Damage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;

@Getter
public class DamageEvent extends AbstractDamageEvent {
    private final Damage damage;
    private final AttackSource attackSource;
    @Setter private boolean isVoid;

    public DamageEvent(LivingEntity victim, Damage damage, boolean isVoid, AttackSource attackSource) {
        super(victim);
        this.damage = damage;
        this.isVoid = isVoid;
        this.attackSource = attackSource;
    }
}
