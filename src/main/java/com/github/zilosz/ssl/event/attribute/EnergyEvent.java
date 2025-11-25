package com.github.zilosz.ssl.event.attribute;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class EnergyEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  @Setter private float energy;

  public EnergyEvent(Player player, float energy) {
    this.player = player;
    this.energy = energy;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
