package com.github.zilosz.ssl.util.entity.finder.selector.implementation;

import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public class DistanceSelector implements EntitySelector {
    private final double distance;

    public DistanceSelector(double distance) {
        this.distance = distance;
    }

    @Override
    public Stream<Entity> getEntityStream(Location source) {
        return source.getWorld().getEntities().stream()
                .filter(entity -> EntityUtils.center(entity).distanceSquared(source) <= this.distance * this.distance);
    }
}
