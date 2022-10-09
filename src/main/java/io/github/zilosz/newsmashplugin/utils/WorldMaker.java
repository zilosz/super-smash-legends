package io.github.zilosz.newsmashplugin.utils;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class WorldMaker {

    public static void create(String name, File schematic, Vector vector) throws IOException {
        WorldCreator config = WorldCreator.name(name).type(WorldType.FLAT).generateStructures(false).generatorSettings("3;minecraft:air;2");
        ClipboardFormat format = Objects.requireNonNull(ClipboardFormats.findByFile(schematic));
        format.load(schematic).paste(new BukkitWorld(Bukkit.createWorld(config)), vector);
    }

    public static void create(String name, File schematic, double x, double y, double z) throws IOException {
        create(name, schematic, new Vector(x, y, z));
    }

    public static boolean delete(String name) throws IOException {
        File world = new File(Bukkit.getWorldContainer(), name);

        if (world.exists()) {
            FileUtils.deleteDirectory(world);
            return true;
        }

        return false;
    }
}
