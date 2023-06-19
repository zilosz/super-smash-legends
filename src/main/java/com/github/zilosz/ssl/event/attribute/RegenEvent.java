package com.github.zilosz.ssl.event.attribute;

import com.github.zilosz.ssl.event.CustomEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RegenEvent extends CustomEvent {
    @Getter private final Player player;
    @Setter @Getter private double regen;

    public RegenEvent(Player player, double regen) {
        this.player = player;
        this.regen = regen;
    }

    public static boolean attempt(Player player, double regen) {
        if (player.getHealth() == 20) return false;

        RegenEvent event = new RegenEvent(player, regen);
        Bukkit.getPluginManager().callEvent(event);
        player.setHealth(Math.min(20, player.getHealth() + event.getRegen()));

        return true;
    }
}
