package com.github.zilosz.ssl.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
@RequiredArgsConstructor
public class PotionEffectEvent extends Event implements Cancellable {
  private static final HandlerList HANDLERS = new HandlerList();

  private final LivingEntity entity;
  private final PotionEffectType type;
  private final int duration;
  private final int amplifier;
  @Setter private boolean cancelled;

  public PotionEffectEvent(LivingEntity entity, PotionEffect effect) {
    this(entity, effect.getType(), effect.getDuration(), effect.getAmplifier());
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  public void apply() {
    Bukkit.getPluginManager().callEvent(this);

    if (!cancelled) {
      entity.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
