package io.github.aura6.supersmashlegends.utils.file;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class YamlReader {

    public static Location readLocation(String world, String location) {
        String[] parts = location.split(":");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        float yaw = parts.length >= 4 ? Float.parseFloat(parts[3]) : 0;
        float pitch = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0;
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public static List<Location> readLocations(String world, List<String> locations) {
        return locations.stream().map(loc -> readLocation(world, loc)).collect(Collectors.toList());
    }

    public static Vector readVector(String vector) {
        String[] parts = vector.split(":");
        return new Vector(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }

    public static ItemStack readItemStack(Section section) {
        ItemBuilder<ItemMeta> builder = new ItemBuilder<>(Material.valueOf(section.getString("Material")));
        section.getOptionalString("Name").ifPresent(builder::setName);
        section.getOptionalStringList("Lore").ifPresent(builder::setLore);
        builder.setAmount(section.getOptionalInt("Amount").orElse(1));
        section.getOptionalInt("Data").ifPresent(builder::setData);
        return builder.get();
    }
}
