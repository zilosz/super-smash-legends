package com.github.zilosz.ssl.util.entity.finder.selector.impl;

import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class DistanceSelector implements EntitySelector {
  private final double distance;

  @Override
  public Stream<Entity> getEntityStream(Location source) {
    double distSq = distance * distance;
    return source
        .getWorld()
        .getEntities()
        .stream()
        .filter(entity -> EntityUtils.center(entity).distanceSquared(source) <= distSq);
  }
}
