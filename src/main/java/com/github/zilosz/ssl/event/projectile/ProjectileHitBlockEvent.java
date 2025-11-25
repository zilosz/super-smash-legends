package com.github.zilosz.ssl.event.projectile;

import com.github.zilosz.ssl.projectile.CustomProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class ProjectileHitBlockEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();

  private final CustomProjectile<?> projectile;
  private final BlockHitResult result;
  @Setter private boolean cancelled;

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
