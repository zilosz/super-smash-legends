package com.github.zilosz.ssl.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class RunnableUtils {

  public static void runIntervaledTask(Plugin plugin, int count, int delay, Runnable onRun) {
    runIntervaledTask(plugin, count, delay, onRun, () -> {});
  }

  public static void runIntervaledTask(
      Plugin plugin, int count, int delay, Runnable onRun, Runnable onEnd
  ) {
    for (int i = 0; i < count; i++) {
      Bukkit.getScheduler().runTaskLater(plugin, onRun, (long) i * delay);
    }
    Bukkit.getScheduler().runTaskLater(plugin, onEnd, (long) count * delay + 1);
  }
}
