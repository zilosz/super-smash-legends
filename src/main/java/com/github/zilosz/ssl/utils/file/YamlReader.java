package com.github.zilosz.ssl.utils.file;

import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.Noise;
import com.github.zilosz.ssl.utils.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class YamlReader {

    public static List<Double> numbers(String numbers) {
        return Arrays.stream(numbers.split(":")).map(Double::parseDouble).collect(Collectors.toList());
    }

    public static Location location(String world, String location) {
        List<Double> parts = numbers(location);
        float yaw = parts.size() >= 4 ? parts.get(3).floatValue() : 0;
        float pitch = parts.size() >= 5 ? parts.get(4).floatValue() : 0;
        return new Location(Bukkit.getWorld(world), parts.get(0), parts.get(1), parts.get(2), yaw, pitch);
    }

    public static List<Location> locations(String world, List<String> locations) {
        return locations.stream().map(loc -> location(world, loc)).collect(Collectors.toList());
    }

    public static Vector vector(String vector) {
        List<Double> parts = numbers(vector);
        return new Vector(parts.get(0), parts.get(1), parts.get(2));
    }

    public static ItemStack stack(Section section) {
        ItemBuilder<ItemMeta> builder = new ItemBuilder<>(Material.valueOf(section.getString("Material")));
        section.getOptionalString("Name").ifPresent(builder::setName);
        section.getOptionalStringList("Lore").ifPresent(builder::setLore);
        builder.setAmount(section.getOptionalInt("Amount").orElse(1));
        section.getOptionalInt("Data").ifPresent(builder::setData);
        return builder.get();
    }

    public static Noise noise(Section section) {
        return new Noise(Sound.valueOf(section.getString("Type")), section.getFloat("Volume"), section.getFloat("Pitch"));
    }

    public static double incLin(Section config, String stat, double val, double limit) {
        return MathUtils.increasingLinear(config.getDouble("Min" + stat), config.getDouble("Max" + stat), limit, val);
    }

    public static double decLin(Section config, String stat, double val, double limit) {
        return MathUtils.decreasingLinear(config.getDouble("Min" + stat), config.getDouble("Max" + stat), limit, val);
    }
}
