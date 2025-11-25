package com.github.zilosz.ssl.util.entity.finder.selector;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

@FunctionalInterface
public interface EntitySelector {

  default boolean containsEntity(Location source, Entity entity) {
    return getEntityStream(source).anyMatch(other -> other.equals(entity));
  }

  Stream<Entity> getEntityStream(Location source);
}
