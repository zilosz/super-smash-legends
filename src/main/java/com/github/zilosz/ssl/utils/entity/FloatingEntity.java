package com.github.zilosz.ssl.utils.entity;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public class FloatingEntity<T extends Entity> {
    @Getter private final T entity;
    private final ArmorStand armorStand;

    private FloatingEntity(T entity, ArmorStand armorStand) {
        this.entity = entity;
        this.armorStand = armorStand;
    }

    public void destroy() {
        this.armorStand.remove();
        this.entity.remove();
    }

    public void teleport(Location location) {
        this.armorStand.eject();
        this.armorStand.teleport(location);
        this.armorStand.setPassenger(this.entity);
    }

    public static <T extends Entity> FloatingEntity<T> fromEntity(T entity) {
        ArmorStand armorStand = entity.getWorld().spawn(entity.getLocation(), ArmorStand.class);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setVisible(false);
        armorStand.setPassenger(entity);
        return new FloatingEntity<>(entity, armorStand);
    }
}
