package com.github.zilosz.ssl.utils;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WorldManager {
    private final Map<String, EditSession> worlds = new HashMap<>();

    public void createWorld(String name, File schematic, Vector paste) {
        WorldCreator config = WorldCreator.name(name)
                .type(WorldType.FLAT).generateStructures(false).generatorSettings("3;minecraft:air;2");

        com.sk89q.worldedit.Vector weVector = new com.sk89q.worldedit.Vector(paste.getX(), paste.getY(), paste.getZ());

        try {
            Schematic schem = ClipboardFormat.SCHEMATIC.load(schematic);
            EditSession editSession = schem.paste(new BukkitWorld(Bukkit.createWorld(config)), weVector);
            this.worlds.put(name, editSession);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetWorld(String name) {
        Optional.ofNullable(this.worlds.remove(name)).ifPresent(session -> session.undo(session));
    }
}
