package io.github.aura6.supersmashlegends.utils;

import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Noise {
    @Setter private Sound sound;
    @Setter private float volume;
    @Setter private float pitch;

    public Noise(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playForPlayer(Player player, Location location) {
        player.playSound(location, sound, volume, pitch);
    }

    public void playForPlayer(Player player) {
        playForPlayer(player, player.getLocation());
    }

    public void playForAll(Location location) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }
}
