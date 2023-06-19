package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.damage.DamageSettings;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

@Getter
public class AttributeDamageEvent extends DamageEvent {
    private final Attribute attribute;

    public AttributeDamageEvent(LivingEntity victim, DamageSettings damageSettings, boolean isVoid, Attribute attribute) {
        super(victim, damageSettings, isVoid);
        this.attribute = attribute;
    }
}
