package io.github.aura6.supersmashlegends.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class RegenEvent extends CustomEvent {
    @Getter private final Player player;
    @Setter @Getter private double regen;

    public RegenEvent(Player player, double regen) {
        this.player = player;
        this.regen = regen;
    }
}
