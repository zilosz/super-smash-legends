package com.github.zilosz.ssl.utils.entity.finder.selector.implementation;

import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public class HitBoxSelector implements EntitySelector {
    private final double x;
    private final double y;
    private final double z;

    public HitBoxSelector(double hitBox) {
        this(hitBox, hitBox, hitBox);
    }

    public HitBoxSelector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Stream<Entity> getEntityStream(Location location) {
        return location.getWorld().getNearbyEntities(location, this.x, this.y, this.z).stream();
    }
}
