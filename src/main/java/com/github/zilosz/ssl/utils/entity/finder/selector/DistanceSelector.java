package com.github.zilosz.ssl.utils.entity.finder.selector;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public class DistanceSelector implements EntitySelector {
    private final double distance;

    public DistanceSelector(double distance) {
        this.distance = distance;
    }

    @Override
    public Stream<Entity> getEntityStream(Location location) {
        return location.getWorld().getEntities().stream()
                .filter(entity -> entity.getLocation().distanceSquared(location) <= distance * distance);
    }
}
