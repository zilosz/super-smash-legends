package com.github.zilosz.ssl.utils.entity;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public abstract class FloatingEntity<T extends Entity> {
    @Getter private T entity;
    private ArmorStand armorStand;

    public static <T extends Entity> FloatingEntity<T> fromEntity(T entity) {

        FloatingEntity<T> floatingEntity = new FloatingEntity<>() {

            @Override
            public T createEntity(Location location) {
                return entity;
            }
        };

        floatingEntity.spawn(entity.getLocation());
        return floatingEntity;
    }

    public void spawn(Location location) {
        this.armorStand = location.getWorld().spawn(location, ArmorStand.class);
        this.armorStand.setGravity(false);
        this.armorStand.setMarker(true);
        this.armorStand.setVisible(false);
        this.armorStand.setBasePlate(false);

        this.entity = this.createEntity(location);
        this.armorStand.setPassenger(this.entity);
    }

    public abstract T createEntity(Location location);

    public void destroy() {
        this.armorStand.remove();

        if (this.entity.getType() != EntityType.PLAYER) {
            this.entity.remove();
        }
    }

    public void teleport(Location location) {
        this.armorStand.eject();
        this.armorStand.teleport(location);
        this.armorStand.setPassenger(this.entity);
    }
}
