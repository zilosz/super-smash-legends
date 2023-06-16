package io.github.aura6.supersmashlegends.event.attribute;

import io.github.aura6.supersmashlegends.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class EnergyEvent extends CustomEvent {
    @Getter private final Player player;
    @Getter @Setter private float energy;

    public EnergyEvent(Player player, float energyGained) {
        this.player = player;
        this.energy = energyGained;
    }
}
