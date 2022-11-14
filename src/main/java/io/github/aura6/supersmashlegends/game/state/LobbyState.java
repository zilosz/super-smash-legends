package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.connorlinfoot.titleapi.TitleAPI;
import io.github.aura6.supersmashlegends.Resources;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.utils.HotbarItem;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LobbyState extends GameState {
    private final List<HotbarItem> hotbarItems = new ArrayList<>();

    public LobbyState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "Lobby";
    }

    @Override
    public List<String> getScoreboard(Player player) {

        Replacers replacers = new Replacers()
                .add("CURRENT", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .add("CAP", String.valueOf(plugin.getTeamManager().getPlayerCap()))
                .add("KIT", plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName())
                .add("JEWELS", String.valueOf(plugin.getEconomyManager().getJewels(player)));

        return replacers.replaceLines(Arrays.asList(
                "&5&l---------------------",
                "&7Waiting for players...",
                "",
                "&fPlayers: &5{CURRENT}&7/&f{CAP}",
                "",
                "&fKit: &5{KIT}",
                "&fJewels: &5{JEWELS}",
                "&5&l---------------------"
        ));
    }

    @Override
    public boolean isNotInGame() {
        return true;
    }

    @Override
    public void start() {
        plugin.getArenaManager().setupArenas();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ActionBarAPI.sendActionBar(player, MessageUtils.color("&7Returned to the lobby."));
            initializePlayer(player);

            if (plugin.getGameManager().isSpectator(player)) {
                plugin.getKitManager().setupUser(player);
                plugin.getEconomyManager().setupUser(player);

            } else {
                plugin.getKitManager().setKit(player, plugin.getKitManager().getSelectedKit(player).copy());
            }
        }
    }

    private Location getSpawn() {
        return YamlReader.location("lobby", plugin.getResources().getLobby().getString("Spawn"));
    }

    private void initializePlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlySpeed(0.1f);
        player.setHealth(20);
        player.setExp(0);
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 1);

        player.teleport(getSpawn());

        Resources resources = plugin.getResources();

        hotbarItems.add(resources.loadAndRegisterHotbarItem(
                "KitSelector", player, 8, e -> plugin.getKitSelector().build().open(player)));

        hotbarItems.add(resources.loadAndRegisterHotbarItem(
                "ArenaVoter", player, 7, e -> plugin.getArenaVoter().build().open(player)));

        if (plugin.getTeamManager().getTeamSize() > 1) {
            hotbarItems.add(resources.loadAndRegisterHotbarItem(
                    "TeamSelector", player, 6, e -> plugin.getTeamSelector().build().open(player)));
        }
    }

    @Override
    public void end() {
        Chat.GAME.broadcast("&7The game is starting...");
        plugin.getGameManager().startTicks();

        hotbarItems.forEach(HotbarItem::destroy);
        hotbarItems.clear();

        plugin.getArenaManager().setupArena();
        Arena arena = plugin.getArenaManager().getArena();

        Replacers replacers = new Replacers()
                .add("ARENA", arena.getName())
                .add("AUTHORS", arena.getAuthors());

        List<String> description = replacers.replaceLines(
                plugin.getResources().getConfig().getStringList("Description"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getGameManager().setupProfile(player);
            plugin.getTeamManager().assignPlayer(player);

            description.forEach(player::sendMessage);
        }

        plugin.getTeamManager().removeEmptyTeams();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Chat.GAME.send(player, "&7Please use &d&l/start &7to start and &3&l/end &7to end.");
        initializePlayer(player);

        plugin.getDb().setIfEnabled(player.getUniqueId(), "name", player.getName());
        plugin.getEconomyManager().setupUser(player);
        plugin.getKitManager().setupUser(player);

        event.setJoinMessage(Chat.JOIN.get(String.format("&5%s &7has joined the game.", player.getName())));
        TitleAPI.sendTitle(player, MessageUtils.color("&7Welcome to"), MessageUtils.color("&5&lSuper Smash Legends!"), 10, 40, 10);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
    }

    @EventHandler
    public void handleVoid(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.getEntity().teleport(getSpawn());
        }
    }
}
