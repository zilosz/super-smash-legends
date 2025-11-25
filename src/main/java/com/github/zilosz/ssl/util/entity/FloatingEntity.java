package com.github.zilosz.ssl.util.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FloatingEntity<T extends Entity> {
  @Getter private final T entity;
  private final ArmorStand armorStand;

  public static <T extends Entity> FloatingEntity<T> fromEntity(T entity) {
    ArmorStand armorStand = entity.getWorld().spawn(entity.getLocation(), ArmorStand.class);
    armorStand.setGravity(false);
    armorStand.setMarker(true);
    armorStand.setVisible(false);
    armorStand.setPassenger(entity);
    return new FloatingEntity<>(entity, armorStand);
  }

  public void destroy() {
    armorStand.remove();
    entity.remove();
  }

  public void teleport(Location location) {
    armorStand.eject();
    armorStand.teleport(location);
    armorStand.setPassenger(entity);
  }
}
