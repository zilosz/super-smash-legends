package io.github.zilosz.newsmashplugin.game.state;

import io.github.zilosz.newsmashplugin.NewSmashPlugin;
import io.github.zilosz.newsmashplugin.utils.message.Chatter;
import io.github.zilosz.newsmashplugin.utils.message.Replacer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public abstract class GameState implements Listener {
    protected final NewSmashPlugin plugin;
    protected final Chatter chatter;

    public GameState(NewSmashPlugin plugin) {
        this.plugin = plugin;
        chatter = plugin.getChatter();
    }

    public abstract String getSectionName();

    public abstract List<String> parseScoreboardMessage(List<String> message);

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        chatter.sendMessage(event.getPlayer(), "leave", "message", new Replacer("player", event.getPlayer().getName()));
    }
}
