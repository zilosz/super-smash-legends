package com.github.zilosz.ssl.util.effects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

@RequiredArgsConstructor
public enum ColorType {
  AQUA("Aqua", DyeColor.LIGHT_BLUE, "&b", Color.AQUA),
  BLACK("Black", DyeColor.BLACK, "&0", Color.BLACK),
  BLUE("Blue", DyeColor.BLUE, "&9", Color.BLUE),
  BROWN("Brown", DyeColor.BROWN, "&4", null),
  CYAN("Cyan", DyeColor.CYAN, "&3", Color.fromRGB(0, 170, 170)),
  DARK_BLUE("Dark Blue", DyeColor.BLUE, "&1", Color.fromRGB(0, 0, 170)),
  DARK_GRAY("Dark Gray", DyeColor.GRAY, "&8", Color.fromRGB(85, 85, 85)),
  DARK_RED("Dark Red", DyeColor.RED, "&4", Color.fromRGB(170, 0, 0)),
  FUCHSIA("Fuchsia", DyeColor.MAGENTA, "&d", Color.FUCHSIA),
  GRAY("Gray", DyeColor.GRAY, "&7", Color.GRAY),
  GREEN("Green", DyeColor.GREEN, "&2", Color.GREEN),
  LIGHT_BLUE("Light Blue", DyeColor.LIGHT_BLUE, "&b", null),
  LIME("Lime", DyeColor.LIME, "&a", Color.LIME),
  MAGENTA("Magenta", DyeColor.MAGENTA, "&d", null),
  MAROON("Maroon", DyeColor.BROWN, "&4", Color.MAROON),
  NAVY("Navy", DyeColor.BLUE, "&1", Color.NAVY),
  OLIVE("Olive", DyeColor.GREEN, "&2", Color.OLIVE),
  ORANGE("Orange", DyeColor.ORANGE, "&6", Color.ORANGE),
  PINK("Pink", DyeColor.PINK, "&d", null),
  PURPLE("Purple", DyeColor.PURPLE, "&5", Color.PURPLE),
  RED("Red", DyeColor.RED, "&c", Color.RED),
  SILVER("Silver", DyeColor.SILVER, "&7", Color.SILVER),
  TEAL("Teal", DyeColor.CYAN, "&3", Color.TEAL),
  WHITE("White", DyeColor.WHITE, "&f", Color.WHITE),
  YELLOW("Yellow", DyeColor.YELLOW, "&e", Color.YELLOW);

  @Getter private final String name;
  @Getter private final DyeColor dyeColor;
  @Getter private final String chatSymbol;
  private final Color bukkitColor;

  public java.awt.Color getAwtColor() {
    Color realColor = getBukkitColor();
    return new java.awt.Color(realColor.getRed(), realColor.getGreen(), realColor.getBlue());
  }

  public Color getBukkitColor() {
    return bukkitColor == null ? dyeColor.getColor() : bukkitColor;
  }
}
