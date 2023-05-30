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

    public static String progressBar(String fullChar, String emptyChar, String fullColor, String emptyColor, double full, double total, int charCount) {
        int fullCount = (int) (charCount * full / total);
        String fullBars = fullColor + StringUtils.repeat(fullChar, fullCount);
        String emptyBars = emptyColor + StringUtils.repeat(emptyChar, charCount - fullCount);
        return color(fullBars + emptyBars);
    }
}
