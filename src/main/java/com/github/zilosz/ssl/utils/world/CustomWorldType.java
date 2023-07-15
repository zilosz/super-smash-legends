package com.github.zilosz.ssl.utils.world;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

@Getter
public enum CustomWorldType {
    LOBBY("lobby"),
    ARENA("arena");

    private final String worldName;

    CustomWorldType(String worldName) {
        this.worldName = worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }
}
