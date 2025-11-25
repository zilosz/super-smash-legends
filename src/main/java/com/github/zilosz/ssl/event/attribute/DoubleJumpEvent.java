package com.github.zilosz.ssl.event.attribute;

import com.github.zilosz.ssl.util.Noise;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class DoubleJumpEvent extends Event implements Cancellable {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  @Setter private double power;
  @Setter private double height;
  @Setter private Noise noise;
  @Setter private boolean cancelled;

  public DoubleJumpEvent(Player player, double power, double height, Noise noise) {
    this.player = player;
    this.power = power;
    this.height = height;
    this.noise = noise;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
