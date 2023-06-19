package com.github.zilosz.ssl.utils.message;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

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

    Chat(String prefix) {
        this.prefix = prefix;
    }

    public String get(String message) {
        return MessageUtils.color(this.prefix + message);
    }

    public void send(CommandSender player, String message) {
        player.sendMessage(this.get(message));
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(this.get(message));
    }
}
