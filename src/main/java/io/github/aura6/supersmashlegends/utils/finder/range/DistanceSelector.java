package io.github.aura6.supersmashlegends.utils.finder.range;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public class DistanceSelector implements RangeSelector {
    private final double distance;

    public DistanceSelector(double distance) {
        this.distance = distance;
    }

    @Override
    public Stream<Entity> getEntityStream(Location location) {
        return location.getWorld().getEntities().stream().filter(entity -> entity.getLocation().distanceSquared(location) <= distance * distance);
    }
}
