package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attack.AttackSource;
import com.github.zilosz.ssl.attack.Damage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

@Getter
public class DamageEvent extends AbstractDamageEvent {
  private static final HandlerList HANDLERS = new HandlerList();
  
  private final Damage damage;
  private final AttackSource attackSource;
  @Setter private boolean isVoid;

  public DamageEvent(
      LivingEntity victim, Damage damage, boolean isVoid, AttackSource attackSource
  ) {
    super(victim);
    this.damage = damage;
    this.isVoid = isVoid;
    this.attackSource = attackSource;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
