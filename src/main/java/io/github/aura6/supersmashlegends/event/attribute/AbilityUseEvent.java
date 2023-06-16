package io.github.aura6.supersmashlegends.event.attribute;

import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.event.CustomEvent;
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
