package io.github.zilosz.newsmashplugin.utils;

import com.sk89q.worldedit.Vector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class YamlReader {

    public static Location readLocation(World world, Section section) {
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = section.getOptionalFloat("yaw").orElse(0f);
        float pitch = section.getOptionalFloat("pitch").orElse(0f);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Location readLocation(String worldName, Section section) {
        return readLocation(Bukkit.getWorld(worldName), section);
    }

    public static Vector readWEVector(Section section) {
        return new Vector(section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
    }
}
