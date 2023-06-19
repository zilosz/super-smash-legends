package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.damage.KbSettings;
import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.util.Vector;

import java.util.Optional;

@Getter
public class AttributeKbEvent extends CustomEvent implements Cancellable {
    @Setter private boolean cancelled = false;
    private final LivingEntity victim;
    private final KbSettings kbSettings;
    private final Attribute attribute;

    public AttributeKbEvent(LivingEntity victim, KbSettings kbSettings, Attribute attribute) {
        this.victim = victim;
        this.kbSettings = kbSettings;
        this.attribute = attribute;
    }

    public double getFinalKb() {
        return this.kbSettings.getFinalKb(this.victim);
    }

    public Optional<Vector> getFinalKbVector() {
        return this.kbSettings.getFinalKbVector(this.victim);
    }
}
