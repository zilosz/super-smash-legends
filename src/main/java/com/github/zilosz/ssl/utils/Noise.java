package com.github.zilosz.ssl.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Noise {
    private Sound sound;
    private float volume;
    private float pitch;

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
