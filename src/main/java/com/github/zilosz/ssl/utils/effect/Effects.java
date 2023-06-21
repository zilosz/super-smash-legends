package com.github.zilosz.ssl.utils.effect;

import com.github.zilosz.ssl.utils.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

public class Effects {

    public static Firework launchFirework(Location location, Color color, int power) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect.Builder builder = FireworkEffect.builder();
        fireworkMeta.addEffect(builder.flicker(true).withColor(color).build());
        fireworkMeta.setPower(power);
        firework.setFireworkMeta(fireworkMeta);
        return firework;
    }

    public static void itemBoom(Plugin plugin, Location center, ItemStack stack, double radius, double speed, int streaks) {
        itemBoom(plugin, center, stack, radius, speed, streaks, null);
    }

    public static void itemBoom(Plugin plugin, Location center, ItemStack stack, double radius, double speed, int streaks, BlockFace face) {
        for (int i = 0; i < streaks; i++) {
            Item item = center.getWorld().dropItem(center, stack);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setVelocity(VectorUtils.randVector(face).multiply(speed));
            Bukkit.getScheduler().runTaskLater(plugin, item::remove, (long) (radius / speed));
        }
    }
}
