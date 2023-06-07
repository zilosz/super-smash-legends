package io.github.aura6.supersmashlegends.utils;

import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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

    public void playForPlayer(Player player, Location location) {
        player.playSound(location, this.sound, this.volume, this.pitch);
    }

    public void playForPlayer(Player player) {
        this.playForPlayer(player, player.getLocation());
    }

    public void playForAll(Location location) {
        location.getWorld().playSound(location, this.sound, this.volume, this.pitch);
    }
}
