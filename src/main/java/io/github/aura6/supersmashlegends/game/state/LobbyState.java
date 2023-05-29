package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import io.github.aura6.supersmashlegends.Resources;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.game.GameManager;
import io.github.aura6.supersmashlegends.game.InGameProfile;
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.utils.HotbarItem;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.HologramLines;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LobbyState extends GameState {
    private final Set<HotbarItem> hotbarItems = new HashSet<>();
    private final Set<Hologram> holograms = new HashSet<>();

    private BukkitTask countdownTask;
    private int secUntilStart;

    public LobbyState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "Lobby";
    }

    @Override
    public boolean isInArena() {
        return false;
    }

    @Override
    public List<String> getScoreboard(Player player) {

        Replacers replacers = new Replacers()
                .add("CURRENT", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .add("CAP", String.valueOf(this.plugin.getTeamManager().getPlayerStartCount()));

        try {
            replacers.add("KIT", this.plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
        } catch (NullPointerException ignored) {}

        List<String> lines = new ArrayList<>();
        lines.add("&5&l---------------------");

        if (this.countdownTask == null) {
            lines.add("&7Waiting for players...");

        } else {
            lines.add(String.format("&7Starting in &e&l%d &7seconds.", this.secUntilStart));
        }

        lines.addAll(Arrays.asList(
                "",
                "&fPlayers: &5{CURRENT}&7/&f{CAP}",
                "",
                "&fKit: &5{KIT}",
                "&5&l---------------------"
        ));

        return replacers.replaceLines(lines);
    }

    private void createLeaderboard(String titleName, String statName, String configName) {
        Location location = YamlReader.location("lobby", plugin.getResources().getLobby().getString(configName));
        Hologram hologram = HolographicDisplaysAPI.get(plugin).createHologram(location);
        holograms.add(hologram);
        HologramLines lines = hologram.getLines();

        lines.appendText(MessageUtils.color(String.format("&5&l%s Leaderboard", titleName)));
        lines.appendText(MessageUtils.color("&7------------------"));

        int size = plugin.getResources().getConfig().getInt("LeaderboardSizes." + configName);

        List<String> players = new ArrayList<>();
        List<Integer> stats = new ArrayList<>();

        for (Document doc : plugin.getDb().getDocuments()) {
            String name = doc.getString("name");
            int stat = (int) doc.getOrDefault(statName, 0);

            if (stat == 0) {
                continue;
            }

            boolean added = false;
            int i;

            for (i = 0; i < players.size(); i++) {

                if (stat > stats.get(i)) {
                    players.add(i, name);
                    stats.add(i, stat);

                    if (players.size() > size) {
                        players.remove(players.size() - 1);
                        stats.remove(stats.size() - 1);
                    }

                    added = true;
                    break;
                }
            }

            if (!added && i < size) {
                players.add(name);
                stats.add(stat);
            }
        }

        if (players.isEmpty()) {
            lines.appendText(MessageUtils.color("&fNo data to display..."));

        } else {

            for (int i = 0; i < players.size(); i++) {
                String line = String.format("&5&l%d. &f%s: &e%d", i + 1, players.get(i), stats.get(i));
                lines.appendText(MessageUtils.color(line));
            }
        }

        lines.appendText(MessageUtils.color("&7------------------"));
    }

    private void stopCountdownTask(boolean abrupt) {
        if (this.countdownTask == null) return;

        this.countdownTask.cancel();
        this.countdownTask = null;

        if (abrupt) {
            Chat.GAME.broadcast("&7Not enough players to start.");
        }
    }

    private void tryCountdownStart() {
        if (Bukkit.getOnlinePlayers().size() < this.plugin.getTeamManager().getPlayerStartCount()) return;

        int notifyInterval = this.plugin.getResources().getConfig().getInt("Game.LobbyCountdown.NotifyInterval");
        int totalSec = this.plugin.getResources().getConfig().getInt("Game.LobbyCountdown.Seconds");
        this.secUntilStart = totalSec + 1;

        this.countdownTask = new BukkitRunnable() {
            float pitch = 0.5f;

            @Override
            public void run() {
                secUntilStart--;

                if (secUntilStart == 0) {
                    plugin.getGameManager().advanceState();
                    stopCountdownTask(false);
                    return;
                }

                if (secUntilStart <= 5 || secUntilStart % notifyInterval == 0) {
                    Chat.GAME.broadcast(String.format("&7Starting in &e&l%d &7seconds.", secUntilStart));

                    if (secUntilStart <= 4) {
                        this.pitch += 1.5f / totalSec;
                    }

                    if (secUntilStart != totalSec) {

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.CLICK, 1, this.pitch);
                        }
                    }
                }
            }

        }.runTaskTimer(this.plugin, 0, 20);
    }

    @Override
    public void start() {
        GameManager gameManager = this.plugin.getGameManager();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ActionBarAPI.sendActionBar(player, MessageUtils.color("&7Returned to the lobby."));
            initializePlayer(player);

            KitManager kitManager = this.plugin.getKitManager();

            Optional.ofNullable(kitManager.getSelectedKit(player)).ifPresentOrElse(kit -> kit.equip(player), () -> {
                kitManager.createHolograms(player);
                kitManager.pullUserKit(player);
            });

            kitManager.updateHolograms(player);

            if (gameManager.hasProfile(player)) {
                InGameProfile profile = gameManager.getProfile(player);
                DecimalFormat format = new DecimalFormat("#.#");

                Replacers replacers = new Replacers()
                        .add("RESULT", profile.getGameResult().getHologramString())
                        .add("KIT", profile.getKit().getBoldedDisplayName())
                        .add("KILLS", String.valueOf(profile.getKills()))
                        .add("DEATHS", String.valueOf(profile.getDeaths()))
                        .add("DAMAGE_TAKEN", format.format(profile.getDamageTaken()))
                        .add("DAMAGE_DEALT", format.format(profile.getDamageDealt()));

                String lastGameLoc = this.plugin.getResources().getLobby().getString("LastGame");
                Location lastGameLocation = YamlReader.location("lobby", lastGameLoc);
                Hologram lastGameHolo = HolographicDisplaysAPI.get(this.plugin).createHologram(lastGameLocation);
                this.holograms.add(lastGameHolo);

                replacers.replaceLines(Arrays.asList(
                        "&5&lLast Game",
                        "&7----------------",
                        "&fResult: {RESULT}",
                        "&fKit: {KIT}",
                        "&fKills: &e{KILLS}",
                        "&fDeaths: &e{DEATHS}",
                        "&fDamage Taken: &e{DAMAGE_TAKEN}",
                        "&fDamage Dealt: &e{DAMAGE_DEALT}",
                        "&7----------------"
                )).forEach(line -> lastGameHolo.getLines().appendText(line));

                for (Player other : Bukkit.getOnlinePlayers()) {

                    if (!other.equals(player)) {
                        lastGameHolo.getVisibilitySettings()
                                .setIndividualVisibility(other, VisibilitySettings.Visibility.HIDDEN);
                    }
                }
            }
        }

        this.plugin.getArenaManager().setupArenas();

        createLeaderboard("Win", "wins", "Wins");
        createLeaderboard("Kill", "kills", "Kills");

        gameManager.reset();
        tryCountdownStart();
    }

    private Location getSpawn() {
        return YamlReader.location("lobby", plugin.getResources().getLobby().getString("Spawn"));
    }

    private void initializePlayer(Player player) {
        player.setHealth(20);
        player.setGameMode(GameMode.SURVIVAL);

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
        holograms.forEach(Hologram::delete);
        holograms.clear();

        stopCountdownTask(false);

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
    public void onLobbyJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        initializePlayer(player);

        KitManager kitManager = this.plugin.getKitManager();
        kitManager.createHolograms(player);
        kitManager.pullUserKit(player);
        kitManager.updateHolograms(player);

        this.plugin.getDb().setIfEnabled(player.getUniqueId(), "name", player.getName());

        tryCountdownStart();
    }

    @EventHandler
    public void onLobbyQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.plugin.getArenaManager().wipePlayer(player);
        this.plugin.getTeamManager().wipePlayer(player);

        stopCountdownTask(true);
    }

    @EventHandler
    public void handleVoid(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            event.setCancelled(true);
            event.getEntity().teleport(getSpawn());
        }
    }

    @EventHandler
    public void stopBows(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        for (Attribute attribute : plugin.getKitManager().getSelectedKit((Player) event.getEntity()).getAttributes()) {

            if (attribute instanceof Ability) {
                Ability ability = (Ability) attribute;

                if (ability.getMaterial() == Material.ARROW) {
                    event.setCancelled(true);
                    ability.getHotbarItem().show();
                }
            }
        }
    }
}
