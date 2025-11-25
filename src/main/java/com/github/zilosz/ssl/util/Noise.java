package com.github.zilosz.ssl.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class Noise {
  private final Sound sound;
  private final float volume;
  private final float pitch;

  public void playForAll(Location location) {
    location.getWorld().playSound(location, sound, volume, pitch);
  }

  public void playForPlayer(Player player) {
    player.playSound(player.getLocation(), sound, volume, pitch);
  }
}
