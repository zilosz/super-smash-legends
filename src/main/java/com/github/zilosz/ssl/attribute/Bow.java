package com.github.zilosz.ssl.attribute;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class Bow extends PassiveAbility {
  protected int ticksCharging;
  private int bowSlot;

  @Override
  public String getUseType() {
    return "Charge Bow";
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getPlayer() != player) return;
    if (!event.hasItem() || event.getItem().getType() != Material.BOW) return;
    if (!event.getPlayer().getInventory().contains(Material.ARROW)) return;
    if (!event.getAction().name().contains("RIGHT")) return;
    if (ticksCharging > 0) return;

    ticksCharging = 1;
    bowSlot = event.getPlayer().getInventory().getHeldItemSlot();
    onStart();
  }

  public void onStart() {}

  @Override
  public void run() {
    super.run();

    if (ticksCharging > 0) {
      onChargeTick();
      ticksCharging++;
    }
  }

  public void onChargeTick() {}

  @EventHandler
  public void onShoot(EntityShootBowEvent event) {
    if (event.getEntity() != player) return;

    event.getProjectile().remove();
    onShot(event.getForce());
    finish();
  }

  public void onShot(double force) {}

  public void finish() {
    ticksCharging = 0;
    onFinish();
  }

  public void onFinish() {}

  @EventHandler
  public void onSwitchItemSlot(PlayerItemHeldEvent event) {
    if (event.getPlayer() == player && ticksCharging > 0 &&
        event.getPreviousSlot() == bowSlot) {
      finish();
    }
  }
}
