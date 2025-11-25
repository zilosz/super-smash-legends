package com.github.zilosz.ssl.util.effects;

import com.github.zilosz.ssl.util.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

public class Effects {

  public static Firework launchFirework(
      Location location,
      FireworkEffect.Builder settings,
      int power
  ) {
    Firework firework = location.getWorld().spawn(location, Firework.class);
    FireworkMeta meta = firework.getFireworkMeta();
    meta.addEffect(settings.build());
    meta.setPower(power);
    firework.setFireworkMeta(meta);
    return firework;
  }

  public static void itemBoom(
      Plugin plugin,
      Location center,
      ItemStack stack,
      double radius,
      double speed,
      int streaks
  ) {
    itemBoom(plugin, center, stack, radius, speed, streaks, null);
  }

  public static void itemBoom(
      Plugin plugin,
      Location center,
      ItemStack stack,
      double radius,
      double speed,
      int streaks,
      BlockFace face
  ) {
    for (int i = 0; i < streaks; i++) {
      Item item = center.getWorld().dropItem(center, stack);
      item.setPickupDelay(Integer.MAX_VALUE);
      item.setVelocity(VectorUtils.randomVector(face).multiply(speed));
      Bukkit.getScheduler().runTaskLater(plugin, item::remove, (long) (radius / speed));
    }
  }
}
