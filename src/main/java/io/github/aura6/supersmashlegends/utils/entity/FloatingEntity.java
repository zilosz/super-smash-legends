package io.github.aura6.supersmashlegends.utils.entity;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public abstract class FloatingEntity {
    private Entity entity;
    private ArmorStand armorStand;

    public abstract Entity createEntity(Location location);

    public void spawn(Location location) {
        if (this.armorStand != null) return;

        this.armorStand = location.getWorld().spawn(location, ArmorStand.class);
        this.armorStand.setGravity(false);
        this.armorStand.setMarker(true);
        this.armorStand.setVisible(false);

        this.entity = this.createEntity(location);
        this.armorStand.setPassenger(this.entity);
    }

    public void destroy() {
        if (this.armorStand == null) return;

        this.entity.remove();
        this.armorStand.remove();
    }
}
