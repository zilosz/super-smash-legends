package io.github.aura6.supersmashlegends.utils.entity.finder.selector;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public class HitBoxSelector implements EntitySelector {
    private final double x;
    private final double y;
    private final double z;

    public HitBoxSelector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public HitBoxSelector(double hitBox) {
        this(hitBox, hitBox, hitBox);
    }

    @Override
    public Stream<Entity> getEntityStream(Location location) {
        return location.getWorld().getNearbyEntities(location, x, y, z).stream();
    }
}
