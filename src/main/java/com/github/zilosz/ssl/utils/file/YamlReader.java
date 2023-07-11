package com.github.zilosz.ssl.utils.file;

import com.github.zilosz.ssl.Resources;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.damage.Damage;
import com.github.zilosz.ssl.damage.KnockBack;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class YamlReader {

    public static List<Location> locations(String worldName, List<String> locations) {
        return locations.stream().map(loc -> location(worldName, loc)).collect(Collectors.toList());
    }

    public static Location location(String worldName, String location) {
        List<Double> parts = doubles(location);
        float yaw = parts.size() >= 4 ? parts.get(3).floatValue() : 0;
        float pitch = parts.size() >= 5 ? parts.get(4).floatValue() : 0;
        return new Location(Bukkit.getWorld(worldName), parts.get(0), parts.get(1), parts.get(2), yaw, pitch);
    }

    public static List<Double> doubles(String numbers) {
        return Arrays.stream(numbers.split(":")).map(Double::parseDouble).collect(Collectors.toList());
    }

    public static Vector vector(String vector) {
        List<Double> parts = doubles(vector);
        return new Vector(parts.get(0), parts.get(1), parts.get(2));
    }

    public static HotbarItem giveHotbarItem(String path, Player player, Consumer<PlayerInteractEvent> action) {
        Section config = SSL.getInstance().getResources().getItems().getSection(path);
        HotbarItem hotbarItem = new HotbarItem(player, stack(config), config.getInt("Slot"));
        hotbarItem.setAction(action);
        hotbarItem.register(SSL.getInstance());
        return hotbarItem;
    }

    public static ItemStack stack(Section section) {
        Material material = Material.valueOf(section.getString("Material"));
        ItemBuilder<ItemMeta> builder = new ItemBuilder<>(material);
        section.getOptionalString("Name").ifPresent(builder::setName);
        section.getOptionalStringList("Lore").ifPresent(builder::setLore);
        section.getOptionalInt("Amount").ifPresent(builder::setCount);
        section.getOptionalInt("Data").ifPresent(builder::setData);
        return builder.get();
    }

    public static Noise noise(Section section) {
        Sound sound = Sound.valueOf(section.getString("Type"));
        return new Noise(sound, section.getFloat("Volume"), section.getFloat("Pitch"));
    }

    public static double increasingValue(Section config, String stat, double val, double limit) {
        double min = config.getDouble("Min" + stat);
        double max = config.getDouble("Max" + stat);
        return MathUtils.increasingValue(min, max, limit, val);
    }

    public static double decreasingValue(Section config, String stat, double val, double limit) {
        double min = config.getDouble("Min" + stat);
        double max = config.getDouble("Max" + stat);
        return MathUtils.decreasingValue(min, max, limit, val);
    }

    public static List<Section> sections(Section config) {
        return config.getKeys().stream()
                .map(String.class::cast)
                .map(config::getSection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static PotionEffect potionEffect(Section config) {
        return potionEffect(config, PotionEffectType.getByName(config.getString("Type")));
    }

    public static PotionEffect potionEffect(Section config, PotionEffectType type) {
        int duration = config.getOptionalInt("Duration").orElse(100_000);
        int amplifier = config.getOptionalInt("Amplifier").orElse(1);
        return new PotionEffect(type, duration, amplifier);
    }

    public static Attack attack(Section config) {
        return attack(config, null);
    }

    public static Attack attack(Section config, Vector direction) {
        Resources resources = SSL.getInstance().getResources();
        int defaultImmunity = resources.getConfig().getInt("Damage.DefaultImmunityTicks");
        int immunityTicks = config.getOptionalInt("ImmunityTicks").orElse(defaultImmunity);
        return new Attack(damage(config), knockBack(config, direction), immunityTicks);
    }

    public static Damage damage(Section config) {
        return new Damage(config.getDouble("Damage"), config.getOptionalBoolean("FactorsArmor").orElse(true));
    }

    public static KnockBack knockBack(Section config, Vector direction) {
        Section mainConfig = SSL.getInstance().getResources().getConfig();

        return new KnockBack(
                direction,
                config.getDouble("Kb"),
                config.getDouble("KbY"),
                config.getOptionalBoolean("FactorsKit").orElse(true),
                config.getOptionalBoolean("FactorsHealth").orElse(true),
                config.getOptionalBoolean("IsLinear").orElse(false),
                config.getOptionalBoolean("FactorsPreviousVelocity").orElse(false),
                mainConfig.getDouble("MinKbHealthMultiplier"),
                mainConfig.getDouble("MaxKbHealthMultiplier")
        );
    }
}
