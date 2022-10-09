package io.github.zilosz.newsmashplugin.utils.message;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

    public static String parse(String message, Replacer... replacers) {
        String result = message;

        for (Replacer replacer : replacers) {
            result = replacer.replace(result);
        }

        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static List<String> parse(List<String> lines, Replacer... replacers) {
        return lines.stream().map(line -> parse(line, replacers)).collect(Collectors.toList());
    }
}
