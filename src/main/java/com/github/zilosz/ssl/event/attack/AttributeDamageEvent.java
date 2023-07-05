package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.damage.Damage;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

@Getter
public class AttributeDamageEvent extends DamageEvent {
    private final Attribute attribute;

    public AttributeDamageEvent(LivingEntity victim, Damage damage, boolean isVoid, Attribute attribute) {
        super(victim, damage, isVoid);
        this.attribute = attribute;
    }
}
