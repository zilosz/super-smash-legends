package io.github.aura6.supersmashlegends.event.attack;

import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.damage.KbSettings;
import io.github.aura6.supersmashlegends.event.CustomEvent;
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
