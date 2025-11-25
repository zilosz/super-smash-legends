package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.Damage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

@Getter
public class AttackEvent extends AbstractDamageEvent {
  private static final HandlerList HANDLERS = new HandlerList();

  private final Attack attack;
  private final AttackInfo info;
  @Setter private boolean cancelled;

  public AttackEvent(LivingEntity victim, Attack attack, AttackInfo info) {
    super(victim);
    this.attack = attack;
    this.info = info;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public Damage getDamage() {
    return attack.getDamage();
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}

