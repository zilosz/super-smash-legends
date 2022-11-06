package io.github.aura6.supersmashlegends.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

public class WorldMaker {

    public static void create(String name, File schematic, Vector pasteVector) {
        WorldCreator config = WorldCreator
                .name(name).type(WorldType.FLAT).generateStructures(false).generatorSettings("3;minecraft:air;2");

        com.sk89q.worldedit.Vector weVector = new com.sk89q.worldedit.Vector(
                pasteVector.getX(), pasteVector.getY(), pasteVector.getZ());

        try {
            ClipboardFormat.SCHEMATIC.load(schematic).paste(new BukkitWorld(Bukkit.createWorld(config)), weVector);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void delete(String name) {
        File world = new File(Bukkit.getWorldContainer(), name);

        if (world.exists()) {

            try {
                FileUtils.deleteDirectory(world);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
