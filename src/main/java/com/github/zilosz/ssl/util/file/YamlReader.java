package com.github.zilosz.ssl.util.file;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.Damage;
import com.github.zilosz.ssl.attack.KnockBack;
import com.github.zilosz.ssl.config.Resources;
import com.github.zilosz.ssl.util.HotbarItem;
import com.github.zilosz.ssl.util.ItemBuilder;
import com.github.zilosz.ssl.util.Noise;
import com.github.zilosz.ssl.util.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class YamlReader {

  public static List<Location> locations(String worldName, Collection<String> locations) {
    return locations.stream().map(loc -> location(worldName, loc)).collect(Collectors.toList());
  }

  public static Location location(String worldName, String location) {
    List<Double> parts = doubles(location);
    float yaw = parts.size() >= 4 ? parts.get(3).floatValue() : 0;
    float pitch = parts.size() >= 5 ? parts.get(4).floatValue() : 0;
    World world = Bukkit.getWorld(worldName);
    return new Location(world, parts.get(0), parts.get(1), parts.get(2), yaw, pitch);
  }

  public static List<Double> doubles(String numbers) {
    return Arrays.stream(numbers.split(":")).map(Double::parseDouble).collect(Collectors.toList());
  }

  public static Vector vector(String vector) {
    List<Double> parts = doubles(vector);
    return new Vector(parts.get(0), parts.get(1), parts.get(2));
  }

  public static HotbarItem giveHotbarItem(
      String path, Player player, Consumer<PlayerInteractEvent> action
  ) {
    Section config = SSL.getInstance().getResources().getItems().getSection(path);
    HotbarItem hotbarItem = new HotbarItem(player, stack(config), config.getInt("Slot"));
    hotbarItem.setAction(action);
    hotbarItem.registerAndShow(SSL.getInstance());
    return hotbarItem;
  }

  public static ItemStack stack(Section section) {
    Material material = Material.valueOf(section.getString("Material"));
    ItemBuilder<ItemMeta> builder = new ItemBuilder<>(material);
    section.getOptionalString("Name").ifPresent(builder::setName);
    section.getOptionalStringList("Lore").ifPresent(builder::setLore);
    section.getOptionalInt("Amount").ifPresent(builder::setCount);
    section.getOptionalByte("Data").ifPresent(builder::setData);
    return builder.get();
  }

  public static Noise noise(Section section) {
    Sound sound = Sound.valueOf(section.getString("Type"));
    return new Noise(sound, section.getFloat("Volume"), section.getFloat("Pitch"));
  }

  public static double incVal(Section config, String stat, double val, double limit) {
    double min = config.getDouble("Min" + stat);
    double max = config.getDouble("Max" + stat);
    return MathUtils.incVal(min, max, limit, val);
  }

  public static double decreasingValue(Section config, String stat, double val, double limit) {
    double min = config.getDouble("Min" + stat);
    double max = config.getDouble("Max" + stat);
    return MathUtils.decVal(min, max, limit, val);
  }

  public static List<Section> sections(Section config) {
    return config
        .getRoutesAsStrings(false)
        .stream()
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

  public static Attack attack(Section config, Vector direction, String name) {
    Resources resources = SSL.getInstance().getResources();
    int defaultImmunity = resources.getConfig().getInt("Damage.DefaultImmunityTicks");
    int immunityTicks = config.getOptionalInt("ImmunityTicks").orElse(defaultImmunity);

    double dmg = config.getDouble("Damage");
    boolean armor = config.getOptionalBoolean("FactorsArmor").orElse(true);
    Damage damage = new Damage(dmg, armor);

    Section generalAttackSettings = resources.getConfig().getSection("Damage");

    KnockBack knockBack = new KnockBack(direction,
        config.getDouble("Kb"),
        config.getDouble("KbY"),
        config.getOptionalBoolean("FactorsKit").orElse(true),
        config.getOptionalBoolean("FactorsHealth").orElse(true),
        config.getOptionalBoolean("IsLinear").orElse(false),
        config.getOptionalBoolean("FactorsPreviousVelocity").orElse(false),
        generalAttackSettings.getDouble("MinKbHealthMultiplier"),
        generalAttackSettings.getDouble("MaxKbHealthMultiplier")
    );

    String attackName = config.getOptionalString("Name").orElse(name);

    return new Attack(damage, knockBack, attackName, immunityTicks);
  }
}
