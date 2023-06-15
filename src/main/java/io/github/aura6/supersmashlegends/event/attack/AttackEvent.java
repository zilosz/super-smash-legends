package io.github.aura6.supersmashlegends.event.damage;

import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.damage.AttackSettings;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

@Getter
public class AttributeAttackEvent extends SingleAttackEvent {
    private final AttackSettings attackSettings;

    public AttributeAttackEvent(LivingEntity victim, Attribute attribute, AttackSettings attackSettings) {
        super(victim, attribute);
        this.attackSettings = attackSettings;
    }
}

