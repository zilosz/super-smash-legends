package io.github.aura6.supersmashlegends.utils.message;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

    public static String color(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> color(List<String> lines) {
        return lines.stream().map(MessageUtils::color).collect(Collectors.toList());
    }
}
