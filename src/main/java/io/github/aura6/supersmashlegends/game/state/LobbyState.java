package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.Resources;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class LobbyState extends GameState {

    public LobbyState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "Lobby";
    }

    @Override
    public Replacers getScoreboardReplacers(Player player) {
        return new Replacers()
                .add("CURRENT", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .add("CAP", String.valueOf(plugin.getTeamManager().getPlayerCap()))
                .add("KIT", plugin.getKitManager().getSelectedKit(player).getDisplayName())
                .add("JEWELS", String.valueOf(plugin.getEconomyManager().getJewels(player)));
    }

    @Override
    public void start() {
        super.start();
        Bukkit.getOnlinePlayers().forEach(this::giveLobbyItems);
    }

    private void giveLobbyItems(Player player) {
        Resources resources = plugin.getResources();

        resources.loadAndRegisterHotbarItem("KitSelector", player, 8,
                e -> plugin.getKitSelector().build().open(player));

        resources.loadAndRegisterHotbarItem("ArenaVoter", player, 7,
                e -> plugin.getArenaVoter().build().open(player));

        if (plugin.getTeamManager().getTeamSize() > 1) {
            resources.loadAndRegisterHotbarItem("TeamSelector", player, 6,
                    e -> plugin.getTeamSelector().build().open(player));
        }
    }

    @Override
    public void end() {
        super.end();
        plugin.getArenaManager().setupArena();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlySpeed(0.1f);
        player.setHealth(20);

        String spawnString = plugin.getResources().getLobby().getString("Spawn");
        player.teleport(YamlReader.readLocation("lobby", spawnString));

        plugin.getDb().setIfEnabled(player.getUniqueId(), "name", player.getName());
        plugin.getEconomyManager().setupUser(player);
        plugin.getKitManager().setupUser(player);

        event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined the game.", player.getName())));
        TitleAPI.sendTitle(player, MessageUtils.color("&7Welcome to"), MessageUtils.color("&5&lSuper Smash Legends!"), 10, 40, 10);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);

        giveLobbyItems(player);
    }

    @EventHandler
    public void handleJoinCapacity(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.getOnlinePlayers().size() >= plugin.getTeamManager().getPlayerCap()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "The game is full!");
        }
    }
}
