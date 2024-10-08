package com.github.zilosz.ssl.game.state.implementation;

import com.comphenix.protocol.ProtocolLibrary;
import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.connorlinfoot.titleapi.TitleAPI;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackManager;
import com.github.zilosz.ssl.attack.AttackSource;
import com.github.zilosz.ssl.attack.Damage;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.GameScoreboard;
import com.github.zilosz.ssl.game.InGameProfile;
import com.github.zilosz.ssl.game.PlayerViewerInventory;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.util.HotbarItem;
import com.github.zilosz.ssl.util.SoundCanceller;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.DeathNPC;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.MessageUtils;
import com.github.zilosz.ssl.util.message.Replacers;
import com.github.zilosz.ssl.util.world.CustomWorldType;
import com.google.common.util.concurrent.AtomicDouble;
import com.nametagedit.plugin.NametagEdit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InGameState extends GameState {
    private static final int MAX_SCOREBOARD_SIZE = 15;
    private static final DecimalFormat FORMAT = new DecimalFormat("#.#");

    private final Map<Player, HotbarItem> trackerItems = new HashMap<>();
    private final Map<Player, BukkitTask> trackerTasks = new HashMap<>();
    private final Map<Player, Player> closestTargets = new HashMap<>();
    private final Map<Player, BukkitTask> respawnTasks = new HashMap<>();
    private final Set<BukkitTask> skinRestorers = new HashSet<>();

    private int secLeft;
    private BukkitTask gameTimer;

    private SoundCanceller meleeSoundCanceller;

    @Override
    public boolean allowsSpecCommand() {
        return false;
    }

    @Override
    public boolean allowsKitSelection() {
        return SSL.getInstance().getResources().getConfig().getBoolean("Game.AllowKitSelectionInGame");
    }

    @Override
    public boolean updatesKitSkins() {
        return true;
    }

    @Override
    public List<String> getScoreboard(Player player) {

        List<String> scoreboard = new ArrayList<>(Arrays.asList(
                GameScoreboard.getLine(),
                "&f&lMinutes Left",
                "&e{MIN_LEFT}",
                ""
        ));

        int playerIndex = scoreboard.size();
        Replacers replacers = new Replacers().add("MIN_LEFT", MessageUtils.secToMin(this.secLeft));

        if (SSL.getInstance().getGameManager().isPlayerAlive(player)) {
            scoreboard.add("");
            scoreboard.add("&f&lKit");
            scoreboard.add("{KIT}");

            Optional.ofNullable(SSL.getInstance().getKitManager().getSelectedKit(player))
                    .ifPresent(kit -> replacers.add("KIT", kit.getDisplayName()));
        }

        scoreboard.add(GameScoreboard.getLine());

        TeamManager teamManager = SSL.getInstance().getTeamManager();
        Set<Player> alivePlayers = SSL.getInstance().getGameManager().getAlivePlayers();
        boolean spaceForAllPlayers = scoreboard.size() + alivePlayers.size() <= MAX_SCOREBOARD_SIZE;

        if (teamManager.isTeamsModeEnabled()) {
            scoreboard.add(playerIndex, "&f&lTeams");
            List<Team> aliveTeams = teamManager.getAliveTeams();

            if (spaceForAllPlayers) {

                for (Team team : aliveTeams) {
                    Set<Player> players = team.getPlayers();

                    for (Player teamPlayer : players) {

                        if (SSL.getInstance().getGameManager().isPlayerAlive(teamPlayer)) {
                            String chatSymbol = team.getColorType().getChatSymbol();
                            String text = this.getPlayerLivesText(teamPlayer, chatSymbol);
                            scoreboard.add(playerIndex + 1, text);
                        }
                    }
                }

            } else {
                scoreboard.add(playerIndex + 1, "&e&l" + aliveTeams.size() + " &7teams alive.");
            }

        } else {
            scoreboard.add(playerIndex, "&f&lPlayers");

            if (spaceForAllPlayers) {

                for (Player alivePlayer : alivePlayers) {
                    scoreboard.add(playerIndex + 1, this.getPlayerLivesText(alivePlayer, "&7"));
                }

            } else {
                scoreboard.add(playerIndex + 1, "&e&l" + alivePlayers.size() + " &7players alive.");
            }

        }

        if (!spaceForAllPlayers) {
            scoreboard.addAll(Arrays.asList("", "&f&lLives:", this.getLivesText(player)));
        }

        return replacers.replaceLines(scoreboard);
    }

    private String getPlayerLivesText(Player player, String nameColor) {
        return MessageUtils.color(String.format("%s%s: %s", nameColor, player.getName(), this.getLivesText(player)));
    }

    private String getLivesText(Player player) {
        int lives = SSL.getInstance().getGameManager().getProfile(player).getLives();
        int lifeCap = SSL.getInstance().getResources().getConfig().getInt("Game.Lives");
        double lifePercentage = (double) lives / lifeCap;

        String color;

        if (lifePercentage <= 0.25) {
            color = "&c";

        } else if (lifePercentage <= 0.5) {
            color = "&6";

        } else if (lifePercentage <= 0.75) {
            color = "&e";

        } else {
            color = "&a";
        }

        return color + lives;
    }

    @Override
    public void start() {
        Section timerConfig = SSL.getInstance().getResources().getConfig().getSection("Game.Timer");
        int extraPlayers = Math.max(0, SSL.getInstance().getGameManager().getAlivePlayers().size() - 4);
        this.secLeft = timerConfig.getInt("DefaultSeconds") + extraPlayers * timerConfig.getInt("SecondsPerExtraPlayer");

        this.gameTimer = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (--this.secLeft <= 0) {
                Chat.GAME.broadcast("&7Ran out of time!");
                SSL.getInstance().getGameManager().advanceState();
            }
        }, 20, 20);

        this.meleeSoundCanceller = new SoundCanceller(SSL.getInstance(), "game.player.hurt");
        ProtocolLibrary.getProtocolManager().addPacketListener(this.meleeSoundCanceller);

        List<Location> spawnLocations = SSL.getInstance().getArenaManager().getArena().getSpawnLocations();
        List<Location> spawnsLeft = new ArrayList<>(spawnLocations);

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (SSL.getInstance().getGameManager().isPlayerAlive(player)) {
                Location spawn = this.teleportToSpawnPoint(player, spawnsLeft::contains);
                spawnsLeft.remove(spawn);

                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);

                if (spawnsLeft.isEmpty()) {
                    spawnsLeft = spawnLocations;
                }

                SSL.getInstance().getGameManager().getProfile(player).getKit().activate();

                if (SSL.getInstance().getTeamManager().isTeamsModeEnabled()) {
                    String color = SSL.getInstance().getTeamManager().getPlayerColor(player);
                    NametagEdit.getApi().setPrefix(player, MessageUtils.color(color));
                }

                this.giveTracker(player);
            }

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 0.5f);
            TitleAPI.sendTitle(player, MessageUtils.color("&7The &5game &7has started!"), "", 5, 30, 5);
        }
    }

    private Location teleportToSpawnPoint(Player player, Predicate<Location> spawnPredicate) {
        List<Location> allSpawns = SSL.getInstance().getArenaManager().getArena().getSpawnLocations();
        List<Location> possibleSpawns = allSpawns.stream().filter(spawnPredicate).collect(Collectors.toList());

        Comparator<Location> comparator = Comparator.comparingDouble(loc -> this.getDistanceFromOthers(player, loc));
        Location spawn = Collections.max(possibleSpawns, comparator);

        player.teleport(spawn);

        return spawn;
    }

    private void giveTracker(Player player) {

        this.trackerItems.put(player, YamlReader.giveHotbarItem("PlayerTracker", player, e -> {

            if (SSL.getInstance().getGameManager().isSpectator(player)) {
                new PlayerViewerInventory().build().open(player);

                return;
            }

            Optional.ofNullable(this.closestTargets.get(player)).ifPresentOrElse(target -> {
                String distance = FORMAT.format(EntityUtils.getDistance(player, target));
                String name = SSL.getInstance().getTeamManager().getPlayerColor(target) + target.getName();
                Chat.TRACKER.send(player, String.format("%s &7is &e%s &7blocks away.", name, distance));
            }, () -> Chat.TRACKER.send(player, "&7There are no players to track."));
        }));

        this.trackerTasks.put(player, Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            String arena = CustomWorldType.ARENA.getWorldName();

            if (!player.getWorld().getName().equals(arena)) return;

            List<Player> players = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getWorld().getName().equals(arena))
                    .filter(p -> SSL.getInstance().getGameManager().isPlayerAlive(p))
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                    .filter(p -> p != player)
                    .collect(Collectors.toList());

            String actionBar;

            if (players.isEmpty()) {
                actionBar = "&7No players to track.";
                player.setCompassTarget(player.getLocation());

            } else {
                Comparator<Player> comparator = Comparator.comparingDouble(p -> EntityUtils.getDistance(p, player));
                Player closest = Collections.min(players, comparator);
                this.closestTargets.put(player, closest);

                String name = SSL.getInstance().getTeamManager().getPlayerColor(closest) + closest.getName();
                String distance = FORMAT.format(EntityUtils.getDistance(player, closest));
                actionBar = String.format("%s &7is &e%s &7blocks away.", name, distance);

                player.setCompassTarget(closest.getLocation());
            }

            boolean holding = player.getInventory().getHeldItemSlot() == this.trackerItems.get(player).getSlot();
            boolean playing = player.getGameMode() != GameMode.SPECTATOR;

            if (holding && playing) {
                ActionBarAPI.sendActionBar(player, MessageUtils.color(actionBar));
            }
        }, 0, 5));
    }

    private double getDistanceFromOthers(Player player, Location location) {
        double distance = 0;

        for (Player other : SSL.getInstance().getGameManager().getAlivePlayers()) {

            if (other != player) {
                distance += other.getLocation().distanceSquared(location);
            }
        }

        return distance;
    }

    @Override
    public void end() {
        this.gameTimer.cancel();

        CollectionUtils.removeWhileIterating(this.skinRestorers, BukkitTask::cancel);
        CollectionUtils.removeWhileIteratingOverEntry(this.respawnTasks, this::respawnPlayer, BukkitTask::cancel);

        ProtocolLibrary.getProtocolManager().removePacketListener(this.meleeSoundCanceller);

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.removeTracker(player);
            TitleAPI.clearTitle(player);
            player.closeInventory();

            if (SSL.getInstance().getGameManager().isPlayerAlive(player)) {
                SSL.getInstance().getKitManager().getSelectedKit(player).deactivate();
            }
        }
    }

    @Override
    public boolean isInArena() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return true;
    }

    @Override
    public boolean allowsDamage() {
        return true;
    }

    private void respawnPlayer(Player player) {
        if (!SSL.getInstance().getGameManager().isPlayerAlive(player)) return;

        Location spawn = this.teleportToSpawnPoint(player, loc -> true);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);

        Chat.GAME.send(player, "&7You have &arespawned.");
        player.playSound(spawn, Sound.ENDERMAN_TELEPORT, 3, 2);
        player.playSound(spawn, Sound.LEVEL_UP, 1, 0.8f);

        Kit kit = SSL.getInstance().getKitManager().getSelectedKit(player);
        kit.equip();
        kit.activate();
    }

    private void removeTracker(Player player) {
        Optional.ofNullable(this.trackerItems.remove(player)).ifPresent(HotbarItem::destroy);
        Optional.ofNullable(this.trackerTasks.remove(player)).ifPresent(BukkitTask::cancel);
        this.closestTargets.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

            if (damager instanceof Player && SSL.getInstance().getGameManager().isSpectator((Player) damager)) {
                event.setCancelled(true);

                return;
            }
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL || victim.getType() == EntityType.ARMOR_STAND) {
            event.setCancelled(true);

            return;
        }

        boolean isVoid = event.getCause() == EntityDamageEvent.DamageCause.VOID;
        Damage damageSettings = new Damage(event.getFinalDamage(), true);
        DamageEvent damageEvent = new DamageEvent(victim, damageSettings, isVoid, null);
        Bukkit.getPluginManager().callEvent(damageEvent);
        event.setDamage(damageEvent.getFinalDamage());

        if (damageEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(DamageEvent event) {
        double finalDamage = event.getFinalDamage();
        SSL.getInstance().getDamageManager().updateIndicator(event.getVictim(), finalDamage);

        if (!(event.getVictim() instanceof Player) || SSL.getInstance().getNpcRegistry().isNPC(event.getVictim())) {
            return;
        }

        Player player = (Player) event.getVictim();
        GameManager gameManager = SSL.getInstance().getGameManager();

        boolean isVoid = event.isVoid();

        if (gameManager.isSpectator(player) || player.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);

            if (isVoid) {
                player.teleport(SSL.getInstance().getArenaManager().getArena().getWaitLocation());
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            }

        } else {
            double damageTaken;

            if (event.willDie() || isVoid) {
                damageTaken = player.getHealth();

                event.setCancelled(true);
                this.handleDeath(player, !isVoid, isVoid, event.getAttackSource());

            } else {
                damageTaken = finalDamage;

                Kit kit = SSL.getInstance().getKitManager().getSelectedKit(player);
                kit.getHurtNoise().playForAll(player.getLocation());
            }

            InGameProfile profile = SSL.getInstance().getGameManager().getProfile(player);
            profile.getStats().setDamageTaken(profile.getStats().getDamageTaken() + damageTaken);
        }
    }

    private void handleDeath(Player died, boolean spawnNpc, boolean teleportPlayer, AttackSource attackSource) {

        if (spawnNpc) {
            DeathNPC.spawn(SSL.getInstance(), died);
        }

        GameManager gameManager = SSL.getInstance().getGameManager();
        InGameProfile diedProfile = gameManager.getProfile(died);

        diedProfile.setLives(diedProfile.getLives() - 1);
        diedProfile.getStats().setDeaths(diedProfile.getStats().getDeaths() + 1);

        Kit diedKit = diedProfile.getKit();
        diedKit.destroy();
        diedKit.getDeathNoise().playForAll(died.getLocation());

        String deathMessage;
        Location tpLocation;

        Location waitLocation = SSL.getInstance().getArenaManager().getArena().getWaitLocation();
        String diedName = SSL.getInstance().getTeamManager().getPlayerColor(died) + died.getName();

        AttackManager attackManager = SSL.getInstance().getDamageManager();
        AttackSource realAttackSource = attackSource;

        if (realAttackSource == null) {
            realAttackSource = attackManager.getLastAttackSource(died);
        }

        Player killer = realAttackSource == null ? null : realAttackSource.getAttribute().getPlayer();

        if (killer == null) {
            deathMessage = String.format("%s &7died.", diedName);
            tpLocation = waitLocation;

        } else {
            killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 2, 2);
            killer.playSound(killer.getLocation(), Sound.WOLF_HOWL, 3, 2);

            String killerName = SSL.getInstance().getTeamManager().getPlayerColor(killer) + killer.getName();
            String attackName = realAttackSource.getAttack().getName();

            if (killer.equals(died)) {
                tpLocation = waitLocation;
                deathMessage = String.format("%s &7killed themselves with %s&7.", diedName, attackName);

            } else {
                tpLocation = killer.getLocation();
                deathMessage = String.format("%s &7killed by %s &7with %s&7.", diedName, killerName, attackName);

                InGameProfile killerProfile = gameManager.getProfile(killer);
                killerProfile.getStats().setKills(killerProfile.getStats().getKills() + 1);
            }
        }

        Chat.DEATH.broadcast(deathMessage);

        if (teleportPlayer) {
            died.teleport(tpLocation);
            died.setVelocity(new Vector(0, 0, 0));
            died.playSound(died.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
        }

        attackManager.clearIndicator(died);
        attackManager.clearDamageSource(died);
        attackManager.clearImmunities(died);
        attackManager.clearPlayerCombo(died);

        died.setGameMode(GameMode.SPECTATOR);

        if (diedProfile.getLives() <= 0) {
            died.playSound(died.getLocation(), Sound.WITHER_DEATH, 2, 1);
            String title = MessageUtils.color("&7You have been");
            TitleAPI.sendTitle(died, title, MessageUtils.color("&celiminated!"), 7, 25, 7);
            Chat.DEATH.broadcast(MessageUtils.color(String.format("%s &7has been &celiminated!", diedName)));

            TeamManager teamManager = SSL.getInstance().getTeamManager();
            Team diedTeam = teamManager.getEntityTeam(died);

            gameManager.addSpectator(died);

            if (!diedTeam.isAlive()) {
                diedTeam.setLifespan(gameManager.getTicksActive());

                if (teamManager.hasGameEnded()) {
                    gameManager.advanceState();
                }
            }

        } else {
            String title = MessageUtils.color("&7You &cdied!");
            TitleAPI.sendTitle(died, title, MessageUtils.color("&7Respawning soon..."), 7, 25, 7);
            died.playSound(died.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 1);

            int deathSeconds = SSL.getInstance().getResources().getConfig().getInt("Game.DeathWaitSeconds");
            AtomicInteger secondsLeft = new AtomicInteger(deathSeconds);

            double pitchStep = 1.5 / deathSeconds;
            AtomicDouble pitch = new AtomicDouble(0.5);

            this.respawnTasks.put(died, Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

                if (secondsLeft.get() == 0) {
                    this.respawnPlayer(died);
                    this.respawnTasks.remove(died).cancel();
                    return;
                }

                String message = String.format("&7Respawning in &e&l%d &7seconds.", secondsLeft.get());
                ActionBarAPI.sendActionBar(died, MessageUtils.color(message));
                died.playSound(died.getLocation(), Sound.ENDERDRAGON_HIT, 2, (float) pitch.get());

                pitch.set(pitch.get() + pitchStep);
                secondsLeft.set(secondsLeft.get() - 1);
            }, 40, 20));
        }
    }

    @EventHandler
    public void onJoinInGame(PlayerJoinEvent event) {
        this.giveTracker(event.getPlayer());
    }

    @EventHandler
    public void onQuitInGame(PlayerQuitEvent event) {
        this.removeTracker(event.getPlayer());
    }
}
