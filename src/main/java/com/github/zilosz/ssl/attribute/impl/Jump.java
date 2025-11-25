package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class Jump extends Attribute {
  @Getter @Setter private int maxCount;
  private int countLeft;
  @Nullable private BukkitTask hitGroundTask;

  @Override
  public void activate() {
    super.activate();

    player.setFlying(false);
    player.setAllowFlight(true);

    maxCount = kit.getJumpCount();
    countLeft = kit.getJumpCount();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    player.setFlying(false);
    player.setAllowFlight(false);
  }

  public void giveExtraJumps(int count) {
    if (countLeft + count <= maxCount) {
      countLeft += count;
      player.setAllowFlight(true);
    }
  }

  @EventHandler
  public void onToggleFlight(PlayerToggleFlightEvent event) {
    if (event.getPlayer() != player) return;

    event.setCancelled(true);

    DoubleJumpEvent jumpEvent =
        new DoubleJumpEvent(player, kit.getJumpPower(), kit.getJumpHeight(), kit.getJumpNoise());

    Bukkit.getPluginManager().callEvent(jumpEvent);

    if (jumpEvent.isCancelled()) {
      return;
    }

    Vector direction = player.getLocation().getDirection();
    Vector velocity = direction.multiply(jumpEvent.getPower()).setY(jumpEvent.getHeight());

    if (((Entity) player).isOnGround()) {
      double boost = SSL.getInstance().getResources().getConfig().getDouble("JumpGroundBooster");
      velocity.setY(velocity.getY() + boost);
    }

    player.setVelocity(velocity);

    jumpEvent.getNoise().playForAll(player.getLocation());

    if (--countLeft == 0) {
      player.setAllowFlight(false);
    }

    if (hitGroundTask == null) {
      hitGroundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
        if (EntityUtils.isPlayerGrounded(player)) {
          replenish();
        }
      }, 0, 0);
    }
  }

  public void replenish() {
    countLeft = maxCount;
    player.setAllowFlight(true);

    if (hitGroundTask != null) {
      hitGroundTask.cancel();
      hitGroundTask = null;
    }
  }
}
