package com.github.zilosz.ssl.utils.entity.finder.selector;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

public interface EntitySelector {

    Stream<Entity> getEntityStream(Location location);
}
