package io.github.aura6.supersmashlegends.power;

import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;

public class PowerCrystal {
    private final Location physicalLocation;
    @Getter private final Location occupiedLocation;
    @Getter private final Class<? extends Ability> power;
    @Getter private final PowerInfo powerInfo;
    @Getter private final Entity entity;

    public PowerCrystal(Location physicalLocation, Location occupiedLocation, Class<? extends Ability> power, PowerInfo powerInfo) {
        this.physicalLocation = physicalLocation;
        this.occupiedLocation = occupiedLocation;
        this.power = power;
        this.powerInfo = powerInfo;

        entity = physicalLocation.getWorld().spawn(physicalLocation, EnderCrystal.class);
        entity.setCustomName(MessageUtils.colorLines(String.format("&5&lPOWER: %s", powerInfo.getName())));
        entity.setCustomNameVisible(true);

        physicalLocation.getBlock().setType(Material.BEACON);
    }

    public double distanceSquared(Entity entity) {
        return entity.getLocation().distanceSquared(physicalLocation);
    }

    public void destroy() {
        physicalLocation.getBlock().setType(Material.AIR);
        entity.remove();
    }
}
