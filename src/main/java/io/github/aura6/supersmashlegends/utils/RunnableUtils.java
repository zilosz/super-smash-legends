package io.github.aura6.supersmashlegends.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class RunnableUtils {

    public static void runTaskWithIntervals(Plugin plugin, int count, int delay, Runnable onRun) {
        for (int i = 0; i < count; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, onRun, (long) i * delay);
        }
    }

    public static void runTaskWithIntervals(Plugin plugin, int count, int delay, Runnable onRun, Runnable onEnd) {
        runTaskWithIntervals(plugin, count, delay, onRun);
        Bukkit.getScheduler().runTaskLater(plugin, onEnd, (long) (count - 1) * delay);
    }
}
