package io.github.aura6.supersmashlegends.utils.message;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum Chat {
    KIT("&b&lKit» "),
    JOIN("&a&lJoin» "),
    QUIT("&c&lQuit» "),
    ARENA("&2&lArena» "),
    TEAM("&6&lTeam» "),
    COMMAND("&1&lCommand» "),
    GAME("&5&lGame» "),
    DEATH("&4&lDeath» "),
    ABILITY("&d&lAbility» "),
    TRACKER("&e&lTracker» ");

    private final String prefix;

    Chat(String prefix) {
        this.prefix = prefix;
    }

    public String get(String message) {
        return MessageUtils.color(prefix + message);
    }

    public void send(Player player, String message) {
        player.sendMessage(get(message));
    }

    public void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> send(player, message));
    }
}
