package com.github.zilosz.ssl.event.attribute;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class RegenEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  @Setter private double regen;

  public RegenEvent(Player player, double regen) {
    this.player = player;
    this.regen = regen;
  }

  public static boolean attempt(Player player, double regen) {
    if (player.getHealth() == player.getMaxHealth()) return false;

    RegenEvent event = new RegenEvent(player, regen);
    Bukkit.getPluginManager().callEvent(event);
    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + event.regen));

    return true;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
