package com.github.zilosz.ssl.util.message;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

  public static List<String> color(List<String> lines) {
    return lines.stream().map(MessageUtils::color).collect(Collectors.toList());
  }

  public static String color(String line) {
    return ChatColor.translateAlternateColorCodes('&', line);
  }

  public static String progressBar(
      String fullChar,
      String emptyChar,
      String fullColor,
      String emptyColor,
      double full,
      double total,
      int charCount
  ) {
    int fullCount = (int) Math.ceil((charCount * full / total));
    String fullBars = fullColor + StringUtils.repeat(fullChar, fullCount);
    String emptyBars = emptyColor + StringUtils.repeat(emptyChar, charCount - fullCount);

    return color(fullBars + emptyBars);
  }

  public static String secToMin(int sec) {
    return String.format("%s:%s", formatZero(sec / 60), formatZero(sec % 60));
  }

  public static String formatZero(int number) {
    if (number == 0) return "00";
    if (Math.abs(number) < 10) return "0" + number;
    return String.valueOf(number);
  }
}
