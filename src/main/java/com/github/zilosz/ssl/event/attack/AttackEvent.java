package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

@Getter
public class AttackEvent extends CustomEvent implements Cancellable {
    private final LivingEntity victim;
    private final Attack attack;
    private final Attribute attribute;
    @Setter private boolean cancelled = false;

    public AttackEvent(LivingEntity victim, Attack attackSettings, Attribute attribute) {
        this.victim = victim;
        this.attack = attackSettings;
        this.attribute = attribute;
    }
}

