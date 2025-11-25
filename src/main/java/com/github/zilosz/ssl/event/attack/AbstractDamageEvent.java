package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attack.Damage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

@Getter
@RequiredArgsConstructor
public abstract class AbstractDamageEvent extends Event implements Cancellable {
  private final LivingEntity victim;
  @Setter private boolean cancelled;

  public boolean willDie() {
    return getNewHealth() <= 0;
  }

  public double getNewHealth() {
    return victim.getHealth() - getFinalDamage();
  }

  public double getFinalDamage() {
    return getDamage().getFinalDamage(victim);
  }

  public abstract Damage getDamage();
}
