package com.github.zilosz.ssl.game.state.implementation;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.github.zilosz.ssl.config.Resources;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.arena.Arena;
import com.github.zilosz.ssl.arena.ArenaVoter;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.database.PlayerData;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.game.InGameProfile;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.kit.KitSelector;
import com.github.zilosz.ssl.team.TeamSelector;
import com.github.zilosz.ssl.util.HotbarItem;
import com.github.zilosz.ssl.util.Skin;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.MessageUtils;
import com.github.zilosz.ssl.util.message.Replacers;
import com.github.zilosz.ssl.util.world.CustomWorldType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.HologramLines;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LobbyState extends GameState {
    private final Set<HotbarItem> hotbarItems = new HashSet<>();
    private final Set<Hologram> holograms = new HashSet<>();

    private BukkitTask countdownTask;
    private int secUntilStart;
    private boolean isCounting = false;
    private float pitch;

    @Override
    public boolean allowsSpecCommand() {
        return true;
    }

    @Override
    public boolean allowsKitSelection() {
        return true;
    }

    @Override
    public boolean updatesKitSkins() {
        return false;
    }

    @Override
    public List<String> getScoreboard(Player player) {

        Replacers replacers = new Replacers()
                .add("CURRENT", this.getParticipantCount())
                .add("CAP", SSL.getInstance().getResources().getConfig().getInt("Game.MinPlayersToStart"));

        try {
            replacers.add("KIT", SSL.getInstance().getKitManager().getSelectedKit(player).getDisplayName());
        } catch (NullPointerException ignored) {}

        List<String> lines = new ArrayList<>(Arrays.asList(GameScoreboard.getLine(), "&f&lStatus"));

        if (this.isCounting) {
            lines.add(String.format("&7Starting in &e&l%d &7sec", this.secUntilStart));

        } else {
            lines.add("&7Waiting for players");
        }

        lines.addAll(Arrays.asList(
                "",
                "&f&lPlayers &lNeeded",
                "&5&l{CURRENT} &7/ &f{CAP}",
                "",
                "&f&lKit",
                "{KIT}",
                GameScoreboard.getLine()
        ));

        return replacers.replaceLines(lines);
    }

    private int getParticipantCount() {
        GameManager gameManager = SSL.getInstance().getGameManager();
        return (int) Bukkit.getOnlinePlayers().stream().filter(p -> !gameManager.willSpectate(p)).count();
    }

    @Override
    public void start() {
        GameManager gameManager = SSL.getInstance().getGameManager();

        for (Player player : Bukkit.getOnlinePlayers()) {
            KitManager kitManager = SSL.getInstance().getKitManager();

            Optional.ofNullable(kitManager.getSelectedKit(player)).ifPresentOrElse(kit -> {
                kit.equip(player);
            }, () -> {
                kitManager.createHolograms(player);
                kitManager.loadAndSetUserKit(player);
            });

            kitManager.updateHolograms(player);

            if (gameManager.isSpectator(player)) {
                this.initializePlayer(player);
                continue;
            }

            Skin realSkin = kitManager.getRealSkin(player);
            realSkin.applyAcrossTeleport(SSL.getInstance(), player, () -> this.initializePlayer(player));

            InGameProfile profile = gameManager.getProfile(player);
            DecimalFormat format = new DecimalFormat("#.#");

            Replacers replacers = new Replacers().add("KIT", profile.getKit().getBoldedDisplayName())
                    .add("RESULT", profile.getGameResult().getHologramString())
                    .add("KILLS", profile.getStats().getKills())
                    .add("DEATHS", profile.getStats().getDeaths())
                    .add("DAMAGE_TAKEN", format.format(profile.getStats().getDamageTaken()))
                    .add("DAMAGE_DEALT", format.format(profile.getStats().getDamageDealt()));

            String lastGameLoc = SSL.getInstance().getResources().getLobby().getString("LastGame");
            Location lastGameLocation = YamlReader.location(CustomWorldType.LOBBY.getWorldName(), lastGameLoc);
            Hologram lastGameHolo = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(lastGameLocation);
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

            lastGameHolo.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            lastGameHolo.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
        }

        SSL.getInstance().getArenaManager().setupArenas();
        SSL.getInstance().getTeamManager().setupTeams();

        this.createLeaderboard("Win", "Wins", playerData -> playerData.getGameResultStats().getWins());
        this.createLeaderboard("Kill", "Kills", playerData -> playerData.getInGameStats().getKills());

        gameManager.reset();

        if (this.hasEnoughPlayersToStart(this.getParticipantCount())) {
            this.startCountdown();
        }
    }

    private void initializePlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.teleport(this.getSpawn());
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);

        if (!SSL.getInstance().getGameManager().isSpectator(player)) {
            ActionBarAPI.sendActionBar(player, MessageUtils.color("&7Returned to the lobby."));
        }

        Consumer<PlayerInteractEvent> kitAction = e -> new KitSelector().build().open(player);
        this.hotbarItems.add(YamlReader.giveHotbarItem("KitSelector", player, kitAction));

        Consumer<PlayerInteractEvent> arenaAction = e -> new ArenaVoter().build().open(player);
        this.hotbarItems.add(YamlReader.giveHotbarItem("ArenaVoter", player, arenaAction));

        if (SSL.getInstance().getTeamManager().isTeamsModeEnabled()) {
            Consumer<PlayerInteractEvent> teamAction = e -> new TeamSelector().build().open(player);
            this.hotbarItems.add(YamlReader.giveHotbarItem("TeamSelector", player, teamAction));
        }
    }

    private void createLeaderboard(String title, String configPath, Function<PlayerData, Integer> statProvider) {
        Resources resources = SSL.getInstance().getResources();
        String worldName = CustomWorldType.LOBBY.getWorldName();
        Location location = YamlReader.location(worldName, resources.getLobby().getString(configPath));

        Hologram hologram = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(location);
        this.holograms.add(hologram);
        HologramLines lines = hologram.getLines();

        lines.appendText(MessageUtils.color(String.format("&5&l%s Leaderboard", title)));
        lines.appendText(MessageUtils.color("&7------------------"));

        List<PlayerData> allPlayerData = SSL.getInstance().getPlayerDatabase().findAllPlayerData()
                .filter(playerData -> statProvider.apply(playerData) > 0)
                .sorted(Comparator.comparingInt(data -> -statProvider.apply(data)))
                .limit(SSL.getInstance().getResources().getConfig().getInt("LeaderboardSizes." + configPath))
                .collect(Collectors.toList());

        if (allPlayerData.isEmpty()) {
            lines.appendText(MessageUtils.color("&fNo data to display..."));

        } else {
            for (int i = 0; i < allPlayerData.size(); i++) {
                String name = allPlayerData.get(i).getName();
                int stat = statProvider.apply(allPlayerData.get(i));
                String line = String.format("&5&l%d. &f%s: &e%d", i + 1, name, stat);
                lines.appendText(MessageUtils.color(line));
            }
        }

        lines.appendText(MessageUtils.color("&7------------------"));
    }

    private boolean hasEnoughPlayersToStart(int participantCount) {
        return participantCount >= this.getGameConfig().getInt("MinPlayersToStart");
    }

    private void startCountdown() {
        Section countdownConfig = this.getGameConfig().getSection("LobbyCountdown");
        int notifyInterval = countdownConfig.getInt("NotifyInterval");
        int totalSec = countdownConfig.getInt("Seconds");
        int notifyThreshold = countdownConfig.getInt("NotifyThreshold");

        this.secUntilStart = totalSec + 1;
        this.isCounting = true;
        this.pitch = 0.5f;

        this.countdownTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (--this.secUntilStart == 0) {
                SSL.getInstance().getGameManager().advanceState();
                return;
            }

            if (this.secUntilStart <= notifyThreshold || this.secUntilStart % notifyInterval == 0) {
                String message = "&7Starting in &e&l%d &7seconds.";
                Chat.GAME.broadcast(String.format(message, this.secUntilStart));

                if (this.secUntilStart < notifyThreshold) {
                    this.pitch += 1.5f / totalSec;
                }

                if (this.secUntilStart != totalSec) {

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.CLICK, 1, this.pitch);
                    }
                }
            }
        }, 0, 20);
    }

    private Location getSpawn() {
        String spawnString = SSL.getInstance().getResources().getLobby().getString("Spawn");
        return YamlReader.location(CustomWorldType.LOBBY.getWorldName(), spawnString);
    }

    private Section getGameConfig() {
        return SSL.getInstance().getResources().getConfig().getSection("Game");
    }

    @Override
    public void end() {
        CollectionUtils.removeWhileIterating(this.holograms, Hologram::delete);
        CollectionUtils.removeWhileIterating(this.hotbarItems, HotbarItem::destroy);

        if (this.isCounting) {
            this.stopCountdownTask();
        }

        Chat.GAME.broadcast("&7The game is starting...");

        SSL.getInstance().getArenaManager().setupArena();
        Arena arena = SSL.getInstance().getArenaManager().getArena();

        Replacers replacers = new Replacers().add("ARENA", arena.getName()).add("AUTHORS", arena.getAuthors());

        Resources resources = SSL.getInstance().getResources();
        List<String> description = replacers.replaceLines(resources.getConfig().getStringList("Description"));

        GameManager gameManager = SSL.getInstance().getGameManager();
        gameManager.startTicks();

        for (Player player : Bukkit.getOnlinePlayers()) {
            description.forEach(player::sendMessage);

            if (gameManager.willSpectate(player)) {
                gameManager.addSpectator(player);
                SSL.getInstance().getKitManager().getSelectedKit(player).destroy();

            } else {
                gameManager.setupProfile(player);
                SSL.getInstance().getTeamManager().assignPlayer(player);
            }
        }

        SSL.getInstance().getTeamManager().removeEmptyTeams();
    }

    @Override
    public boolean isInArena() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean allowsDamage() {
        return false;
    }

    private void stopCountdownTask() {
        this.isCounting = false;
        this.countdownTask.cancel();
    }

    @EventHandler
    public void onLobbyJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.initializePlayer(player);

        KitManager kitManager = SSL.getInstance().getKitManager();
        kitManager.createHolograms(player);
        kitManager.loadAndSetUserKit(player);
        kitManager.updateHolograms(player);

        if (!this.isCounting && this.hasEnoughPlayersToStart(this.getParticipantCount())) {
            this.startCountdown();
        }
    }

    @EventHandler
    public void onLobbyQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        SSL.getInstance().getArenaManager().removeVote(player);
        SSL.getInstance().getTeamManager().removeEntityFromTeam(player);

        if (this.isCounting && !this.hasEnoughPlayersToStart(this.getParticipantCount() - 1)) {
            this.stopCountdownTask();
            Chat.GAME.broadcast("&7Not enough players to start.");
        }
    }

    @EventHandler
    public void stopBows(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Kit kit = SSL.getInstance().getKitManager().getSelectedKit((Player) event.getEntity());

        for (Attribute attribute : kit.getAttributes()) {

            if (attribute instanceof Ability) {
                Ability ability = (Ability) attribute;

                if (ability.getHotbarItem().getItemStack().getType() == Material.ARROW) {
                    event.setCancelled(true);
                    ability.getHotbarItem().show();

                    break;
                }
            }
        }
    }

    @EventHandler
    public void onLobbyDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.teleport(this.getSpawn());
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
        }
    }
}
