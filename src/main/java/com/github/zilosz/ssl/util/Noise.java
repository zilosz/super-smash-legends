package com.github.zilosz.ssl.util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
public class Noise {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public Noise(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playForAll(Location location) {
        location.getWorld().playSound(location, this.sound, this.volume, this.pitch);
    }

    public void playForPlayer(Player player) {
        player.playSound(player.getLocation(), this.sound, this.volume, this.pitch);
    }
}
