package com.github.zilosz.ssl.util.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;

@Getter
@RequiredArgsConstructor
public enum CustomWorldType {
  LOBBY("lobby"), ARENA("arena");

  private final String worldName;

  public World getWorld() {
    return Bukkit.getWorld(worldName);
  }
}
