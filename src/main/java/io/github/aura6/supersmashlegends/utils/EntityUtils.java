package io.github.aura6.supersmashlegends.utils;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EntityUtils {

    public static boolean isPlayerGrounded(Player player) {
        return player.getLocation().subtract(0, 0.5, 0).getBlock().getType().isSolid();
    }

    public static Location top(LivingEntity entity) {
        return entity.getLocation().add(0, height(entity), 0);
    }

    public static double height(Entity entity) {
        AxisAlignedBB bb = ((CraftEntity) entity).getHandle().getBoundingBox();
        return bb.e - bb.b;
    }

    public static Location center(Entity entity) {
        AxisAlignedBB bb = ((CraftEntity) entity).getHandle().getBoundingBox();
        return new Location(entity.getWorld(), (bb.d + bb.a) / 2, (bb.e + bb.b) / 2, (bb.f + bb.c) / 2);
    }

    public static Location underHand(Entity entity, double distance) {
        Location loc = entity.getLocation();
        loc.setYaw(loc.getYaw() + 90);
        loc.setPitch(0);
        return loc.add(loc.getDirection().multiply(0.42)).add(0, 0.7 - distance, 0);
    }
}
