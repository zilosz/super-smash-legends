package com.github.zilosz.ssl.utils.entity;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public abstract class FloatingEntity<T extends Entity> {
    @Getter private T entity;
    private ArmorStand armorStand;

    public abstract T createEntity(Location location);

    public void spawn(Location location) {
        this.armorStand = location.getWorld().spawn(location, ArmorStand.class);
        this.armorStand.setGravity(false);
        this.armorStand.setMarker(true);
        this.armorStand.setVisible(false);
        this.armorStand.setBasePlate(false);

        this.entity = this.createEntity(location);
        this.armorStand.setPassenger(this.entity);
    }

    public void destroy() {
        this.entity.remove();
        this.armorStand.remove();
    }

    public void teleport(Location location) {
        this.armorStand.eject();
        this.armorStand.teleport(location);
        this.armorStand.setPassenger(this.entity);
    }
}
