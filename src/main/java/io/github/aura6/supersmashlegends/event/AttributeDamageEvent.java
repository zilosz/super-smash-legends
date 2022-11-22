package io.github.aura6.supersmashlegends.event;

import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;

public class AttributeDamageEvent extends DamageEvent {
    @Getter @Setter private Attribute attribute;

    public AttributeDamageEvent(LivingEntity victim, Damage damage, Attribute attribute) {
        super(victim, damage);
        this.attribute = attribute;
    }
}

