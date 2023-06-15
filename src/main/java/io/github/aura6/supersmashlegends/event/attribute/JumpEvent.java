package io.github.aura6.supersmashlegends.event;

import io.github.aura6.supersmashlegends.utils.Noise;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class JumpEvent extends CustomEvent implements Cancellable {
    @Getter private final Player player;
    @Getter @Setter private double power;
    @Getter @Setter private double height;
    @Getter @Setter private Noise noise;
    @Getter @Setter private boolean cancelled = false;

    public JumpEvent(Player player, double power, double height, Noise noise) {
        this.player = player;
        this.power = power;
        this.height = height;
        this.noise = noise;
    }
}
