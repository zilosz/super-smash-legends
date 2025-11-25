package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ItemGiver extends PassiveAbility {
  private BukkitTask giveTask;

  @Override
  public void activate() {
    super.activate();
    int ticksPerItem = config.getInt("TicksPerItem");

    giveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      ItemStack stack = player.getInventory().getItem(slot);
      int atOnce = config.getInt("AmountAtOnce");

      if (stack == null || stack.getAmount() + atOnce <= config.getInt("MaxAmount")) {
        player.getInventory().addItem(hotbarItem.getItemStack());
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
      }
    }, ticksPerItem, ticksPerItem);
  }

  @Override
  public void deactivate() {
    super.deactivate();
    player.getInventory().remove(hotbarItem.getItemStack());

    if (giveTask != null) {
      giveTask.cancel();
    }
  }

  @Override
  public String getUseType() {
    return "&oPassive";
  }
}
