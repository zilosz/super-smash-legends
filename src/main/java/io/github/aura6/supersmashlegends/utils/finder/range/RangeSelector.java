package io.github.aura6.supersmashlegends.utils.finder.range;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public interface RangeSelector {

    Stream<Entity> getEntityStream(Location location);
}
