package com.github.zilosz.ssl.utils.file;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.HotbarItem;
import com.github.zilosz.ssl.utils.ItemBuilder;
import com.github.zilosz.ssl.utils.Noise;
import com.github.zilosz.ssl.utils.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class YamlReader {

    public static List<Location> getLocations(String world, List<String> locations) {
        return locations.stream().map(loc -> getLocation(world, loc)).collect(Collectors.toList());
    }

    public static Location getLocation(String world, String location) {
        List<Double> parts = getNumbers(location);
        float yaw = parts.size() >= 4 ? parts.get(3).floatValue() : 0;
        float pitch = parts.size() >= 5 ? parts.get(4).floatValue() : 0;
        return new Location(Bukkit.getWorld(world), parts.get(0), parts.get(1), parts.get(2), yaw, pitch);
    }

    public static List<Double> getNumbers(String numbers) {
        return Arrays.stream(numbers.split(":")).map(Double::parseDouble).collect(Collectors.toList());
    }

    public static Vector getVector(String vector) {
        List<Double> parts = getNumbers(vector);
        return new Vector(parts.get(0), parts.get(1), parts.get(2));
    }

    public static HotbarItem giveHotbarItem(String path, Player player, Consumer<PlayerInteractEvent> action) {
        Section config = SSL.getInstance().getResources().getItems().getSection(path);
        HotbarItem hotbarItem = new HotbarItem(player, getStack(config), config.getInt("Slot"));
        hotbarItem.setAction(action);
        hotbarItem.register(SSL.getInstance());
        return hotbarItem;
    }

    public static ItemStack getStack(Section section) {
        Material material = Material.valueOf(section.getString("Material"));
        ItemBuilder<ItemMeta> builder = new ItemBuilder<>(material);
        section.getOptionalString("Name").ifPresent(builder::setName);
        section.getOptionalStringList("Lore").ifPresent(builder::setLore);
        section.getOptionalInt("Amount").ifPresent(builder::setCount);
        section.getOptionalInt("Data").ifPresent(builder::setData);
        return builder.get();
    }

    public static Noise getNoise(Section section) {
        Sound sound = Sound.valueOf(section.getString("Type"));
        return new Noise(sound, section.getFloat("Volume"), section.getFloat("Pitch"));
    }

    public static double getIncreasingValue(Section config, String stat, double val, double limit) {
        double min = config.getDouble("Min" + stat);
        double max = config.getDouble("Max" + stat);
        return MathUtils.getIncreasingValue(min, max, limit, val);
    }

    public static double getDecreasingValue(Section config, String stat, double val, double limit) {
        double min = config.getDouble("Min" + stat);
        double max = config.getDouble("Max" + stat);
        return MathUtils.getDecreasingValue(min, max, limit, val);
    }

    public static List<Section> getSections(Section config) {
        return config.getKeys().stream()
                .map(String.class::cast)
                .map(config::getSection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
