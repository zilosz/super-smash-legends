package com.github.zilosz.ssl.util.entity;

import com.github.zilosz.ssl.util.NmsUtils;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EntityUtils {

  public static boolean isPlayerGrounded(Entity player) {
    Location below = player.getLocation().subtract(0, 0.9, 0);
    boolean isTripleGrounded = below.getBlock().getType().isSolid();
    return isTripleGrounded || player.isOnGround();
  }

  public static Location top(Entity entity) {
    return entity.getLocation().add(0, height(entity), 0);
  }

  public static double height(Entity entity) {
    AxisAlignedBB bb = NmsUtils.getEntity(entity).getBoundingBox();
    return bb.e - bb.b;
  }

  public static Location center(Entity entity) {
    AxisAlignedBB bb = NmsUtils.getEntity(entity).getBoundingBox();
    return new Location(entity.getWorld(), (bb.d + bb.a) / 2, (bb.e + bb.b) / 2, (bb.f + bb.c) / 2);
  }

  public static Location underHand(Entity entity, double distance) {
    Location loc = entity.getLocation();
    loc.setYaw(loc.getYaw() + 90);
    loc.setPitch(0);

    return loc.add(loc.getDirection().multiply(0.42)).add(0, 0.7 - distance, 0);
  }

  public static double distance(Entity a, Entity b) {
    return a.getLocation().distance(b.getLocation());
  }
}
