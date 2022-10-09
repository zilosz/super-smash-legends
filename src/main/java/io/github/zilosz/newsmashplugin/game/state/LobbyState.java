package io.github.zilosz.newsmashplugin.game.state;

import io.github.zilosz.newsmashplugin.NewSmashPlugin;
import io.github.zilosz.newsmashplugin.utils.YamlReader;
import io.github.zilosz.newsmashplugin.utils.message.Replacer;
import io.github.zilosz.newsmashplugin.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class LobbyState extends GameState {
    private final String maxPlayers;
    private final Location spawnLocation;

    public LobbyState(NewSmashPlugin plugin) {
        super(plugin);
        maxPlayers = String.valueOf(plugin.getGame().getTeamManager().getMaxPlayers());
        spawnLocation = YamlReader.readLocation("lobby", plugin.getLobbyConfig().getSection("spawnLocation"));
    }

    @Override
    public String getSectionName() {
        return "lobby";
    }

    @Override
    public List<String> parseScoreboardMessage(List<String> message) {
        String current = String.valueOf(Bukkit.getOnlinePlayers().size());
        return MessageUtils.parse(message, new Replacer("current", current), new Replacer("total", maxPlayers));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(spawnLocation);
        chatter.sendMessage(event.getPlayer(), "join", "message", new Replacer("player", event.getPlayer().getName()));
    }
}
