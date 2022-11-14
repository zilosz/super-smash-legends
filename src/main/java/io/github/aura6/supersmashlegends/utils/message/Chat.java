package io.github.aura6.supersmashlegends.utils.message;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum Chat {
    ECONOMY("&eEconomy>> "),
    KIT("&bKit>> "),
    JOIN("&aJoin>> "),
    QUIT("&cQuit>> "),
    ARENA("&2Arena>> "),
    TEAM("&6Team>> "),
    COMMAND("&1Command>> "),
    GAME("&5Game>> "),
    DEATH("&4Death>> "),
    COOLDOWN("&7Cooldown>> "),
    ABILITY("&dAbility>> "),
    POWER("&8Power>> ");

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
