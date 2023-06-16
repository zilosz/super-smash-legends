package io.github.aura6.supersmashlegends.event.attack;

import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.damage.DamageSettings;
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
