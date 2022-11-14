package io.github.aura6.supersmashlegends.utils.message;

import org.apache.commons.lang.StringUtils;
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

    public static String progressBar(int value, int limit, int totalBlocks, String complete, String incomplete) {
        int filledBlocks = (int) (totalBlocks * ((float) value / limit));
        String completed = StringUtils.repeat(color(complete), filledBlocks);
        String notCompleted = StringUtils.repeat(color(incomplete), totalBlocks - filledBlocks);
        return color(completed + notCompleted);
    }
}
