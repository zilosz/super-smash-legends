package com.github.zilosz.ssl.util.entity.finder.selector.impl;

import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class HitBoxSelector implements EntitySelector {
  private final double x;
  private final double y;
  private final double z;

  public HitBoxSelector(double hitBox) {
    this(hitBox, hitBox, hitBox);
  }

  @Override
  public Stream<Entity> getEntityStream(Location source) {
    return source.getWorld().getNearbyEntities(source, x, y, z).stream();
  }
}
