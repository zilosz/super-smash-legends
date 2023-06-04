package io.github.aura6.supersmashlegends.utils.message;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class MessageUtils {

    public static String colorLines(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> colorLines(List<String> lines) {
        return lines.stream().map(MessageUtils::colorLines).collect(Collectors.toList());
    }

    public static String progressBar(String fullChar, String emptyChar, String fullColor, String emptyColor, double full, double total, int charCount) {
        int fullCount = (int) Math.ceil((charCount * full / total));
        String fullBars = fullColor + StringUtils.repeat(fullChar, fullCount);
        String emptyBars = emptyColor + StringUtils.repeat(emptyChar, charCount - fullCount);
        return colorLines(fullBars + emptyBars);
    }

    public static String formatZero(int number) {
        if (number == 0) return "00";
        if (Math.abs(number) < 10) return "0" + number;
        return String.valueOf(number);
    }

    public static String secToMin(int sec) {
        return String.format("%s:%s", formatZero(sec / 60), formatZero(sec % 60));
    }
}
