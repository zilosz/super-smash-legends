package com.github.zilosz.ssl.util.message;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public enum Chat {
  ABILITY("&d&lAbility» "),
  ARENA("&2&lArena» "),
  COMMAND("&1&lCommand» "),
  DEATH("&4&lDeath» "),
  GAME("&5&lGame» "),
  JOIN("&a&lJoin» "),
  KIT("&b&lKit» "),
  QUIT("&c&lQuit» "),
  TEAM("&6&lTeam» "),
  TRACKER("&e&lTracker» ");

  private final String prefix;

  public void send(CommandSender player, String message) {
    player.sendMessage(get(message));
  }

  public String get(String message) {
    return MessageUtils.color(prefix + message);
  }

  public void broadcast(String message) {
    Bukkit.broadcastMessage(get(message));
  }
}
