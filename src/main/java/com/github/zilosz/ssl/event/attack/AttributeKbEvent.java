package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.damage.KnockBack;
import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.util.Vector;

import java.util.Optional;

@Getter
public class AttributeKbEvent extends CustomEvent implements Cancellable {
    private final LivingEntity victim;
    private final KnockBack kb;
    private final Attribute attribute;
    @Setter private boolean cancelled = false;

    public AttributeKbEvent(LivingEntity victim, KnockBack kb, Attribute attribute) {
        this.victim = victim;
        this.kb = kb;
        this.attribute = attribute;
    }

    public double getFinalKb() {
        return this.kb.getFinalKb(this.victim);
    }

    public Optional<Vector> getFinalKbVector() {
        return this.kb.getFinalKbVector(this.victim);
    }
}
