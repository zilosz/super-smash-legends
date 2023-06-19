package com.github.zilosz.ssl.event.attribute;

import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public class AbilityUseEvent extends CustomEvent implements Cancellable {
    @Getter private final ClickableAbility ability;
    @Getter @Setter private boolean cancelled = false;

    public AbilityUseEvent(ClickableAbility ability) {
        this.ability = ability;
    }
}
