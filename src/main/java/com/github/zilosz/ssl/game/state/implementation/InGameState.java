package com.github.zilosz.ssl.game.state.implementation;

import com.comphenix.protocol.ProtocolLibrary;
import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.connorlinfoot.titleapi.TitleAPI;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.arena.Arena;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.Nameable;
import com.github.zilosz.ssl.damage.Damage;
import com.github.zilosz.ssl.damage.DamageManager;
import com.github.zilosz.ssl.event.attack.AttributeDamageEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.game.InGameProfile;
import com.github.zilosz.ssl.game.PlayerViewerInventory;
import com.github.zilosz.ssl.game.state.GameState;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamManager;
import com.github.zilosz.ssl.utils.HotbarItem;
import com.github.zilosz.ssl.utils.SoundCanceller;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.effects.DeathNPC;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import com.github.zilosz.ssl.utils.message.Replacers;
import com.google.common.util.concurrent.AtomicDouble;
import com.nametagedit.plugin.NametagEdit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.citizensnpcs.api.CitizensAPI;
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
                this.getScoreboardLine(),
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

            try {
                replacers.add("KIT", SSL.getInstance().getKitManager().getSelectedKit(player).getDisplayName());
            } catch (NullPointerException ignored) {
            }
        }

        scoreboard.add(this.getScoreboardLine());

        TeamManager teamManager = SSL.getInstance().getTeamManager();
        int lifeCap = SSL.getInstance().getResources().getConfig().getInt("Game.Lives");
        Set<Player> alivePlayers = SSL.getInstance().getGameManager().getAlivePlayers();

        if (teamManager.getTeamSize() == 1) {
            scoreboard.add(playerIndex, "&f&lPlayers");

            if (scoreboard.size() + alivePlayers.size() <= MAX_SCOREBOARD_SIZE) {

                for (Player alivePlayer : alivePlayers) {
                    scoreboard.add(playerIndex + 1, this.getPlayerLivesText(alivePlayer, lifeCap, "&7"));
                }

            } else {
                scoreboard.add(playerIndex + 1, "&e&l" + alivePlayers.size() + " &7players alive.");
            }

        } else {
            scoreboard.add(playerIndex, "&f&lTeams");
            List<Team> aliveTeams = teamManager.getAliveTeams();

            if (scoreboard.size() + alivePlayers.size() <= MAX_SCOREBOARD_SIZE) {

                for (Team team : aliveTeams) {

                    for (Player p : team.getSortedPlayers()) {

                        if (SSL.getInstance().getGameManager().isPlayerAlive(p)) {
                            String text = this.getPlayerLivesText(p, lifeCap, team.getColorType().getChatSymbol());
                            scoreboard.add(playerIndex + 1, text);
                        }
                    }
                }

            } else {
                scoreboard.add(playerIndex + 1, "&e&l" + aliveTeams.size() + " &7teams alive.");
            }
        }

        return replacers.replaceLines(scoreboard);
    }

    private String getPlayerLivesText(Player player, int lifeCap, String nameColor) {
        int lives = SSL.getInstance().getGameManager().getProfile(player).getLives();
        double lifePercentage = (double) lives / lifeCap;

        String lifeColor;

        if (lifePercentage <= 0.25) {
            lifeColor = "&c";

        } else if (lifePercentage <= 0.5) {
            lifeColor = "&6";

        } else if (lifePercentage <= 0.75) {
            lifeColor = "&e";

        } else {
            lifeColor = "&a";
        }

        return MessageUtils.color(String.format("%s%s: %s%s", nameColor, player.getName(), lifeColor, lives));
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
        Comparator<Location> comparator = Comparator.comparingDouble(Arena::getTotalDistanceToPlayers);

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (SSL.getInstance().getGameManager().isPlayerAlive(player)) {
                Location spawn = Collections.max(spawnsLeft, comparator);
                spawnsLeft.remove(spawn);
                player.teleport(spawn);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);

                if (spawnsLeft.isEmpty()) {
                    spawnsLeft = spawnLocations;
                }

                SSL.getInstance().getGameManager().getProfile(player).getKit().activate();

                if (SSL.getInstance().getTeamManager().getTeamSize() > 1) {
                    String color = SSL.getInstance().getTeamManager().getPlayerColor(player);
                    NametagEdit.getApi().setPrefix(player, MessageUtils.color(color));
                }

                this.giveTracker(player);
            }

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 0.5f);
            TitleAPI.sendTitle(player, MessageUtils.color("&7The &5game &7has started!"), "", 5, 30, 5);
        }
    }

    private void giveTracker(Player player) {

        this.trackerItems.put(player, YamlReader.giveHotbarItem("PlayerTracker", player, e -> {

            if (SSL.getInstance().getGameManager().isSpectator(player)) {
                new PlayerViewerInventory().build().open(player);

            } else {
                Optional.ofNullable(this.closestTargets.get(player)).ifPresentOrElse(target -> {
                    String distance = FORMAT.format(EntityUtils.getDistance(player, target));
                    String name = SSL.getInstance().getTeamManager().getPlayerColor(target) + target.getName();
                    Chat.TRACKER.send(player, String.format("%s &7is &e%s &7blocks away.", name, distance));
                }, () -> {
                    Chat.TRACKER.send(player, "&7There are no players to track.");
                });
            }
        }));

        this.trackerTasks.put(player, Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            if (!player.getWorld().getName().equals("arena")) return;

            List<Player> players = Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(p -> p.getWorld().getName().equals("arena"))
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

            if (player.getInventory().getHeldItemSlot() == this.trackerItems.get(player).getSlot()) {
                ActionBarAPI.sendActionBar(player, MessageUtils.color(actionBar));
            }
        }, 0, 5));
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

        player.teleport(SSL.getInstance().getArenaManager().getArena().getFarthestSpawnFromPlayers());

        Chat.GAME.send(player, "&7You have &arespawned.");
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 2);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0.8f);

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);

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

        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity victim = (LivingEntity) event.getEntity();

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL || victim.getType() == EntityType.ARMOR_STAND) {
            event.setCancelled(true);
            return;
        }

        boolean isVoid = event.getCause() == EntityDamageEvent.DamageCause.VOID;
        Damage damageSettings = new Damage(event.getFinalDamage(), true);
        DamageEvent damageEvent = new DamageEvent(victim, damageSettings, isVoid);
        Bukkit.getPluginManager().callEvent(damageEvent);
        event.setDamage(damageEvent.getFinalDamage());

        if (damageEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(DamageEvent event) {
        double finalDamage = event.getFinalDamage();
        boolean isPlayer = event.getVictim() instanceof Player;

        if (isPlayer && !CitizensAPI.getNPCRegistry().isNPC(event.getVictim())) {
            Player player = (Player) event.getVictim();
            GameManager gameManager = SSL.getInstance().getGameManager();

            if (gameManager.isSpectator(player)) {
                event.setCancelled(true);

                if (event.isVoid()) {
                    player.teleport(SSL.getInstance().getArenaManager().getArena().getWaitLocation());
                    player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                }

                return;
            }

            InGameProfile profile = SSL.getInstance().getGameManager().getProfile(player);
            profile.setDamageTaken(profile.getDamageTaken() + Math.min(player.getHealth(), finalDamage));

            if (event instanceof AttributeDamageEvent) {

                if (event.willDie()) {
                    this.handleDeath(player, true, false, ((AttributeDamageEvent) event).getAttribute(), true);
                    return;
                }

            } else {

                if (event.isVoid()) {
                    this.handleDeath(player, false, true, null, true);
                    event.setCancelled(true);
                    return;
                }

                if (event.willDie()) {
                    this.handleDeath(player, true, false, null, false);
                    event.setCancelled(true);
                    return;
                }
            }

            Kit kit = SSL.getInstance().getKitManager().getSelectedKit(player);
            kit.getHurtNoise().playForAll(player.getLocation());
        }

        SSL.getInstance().getDamageManager().updateIndicator(event.getVictim(), finalDamage);
    }

    private void handleDeath(Player died, boolean spawnNpc, boolean teleportPlayer, Attribute directKillingAttribute, boolean preferAttributeDamage) {

        if (spawnNpc) {
            DeathNPC.spawn(SSL.getInstance(), died);
        }

        GameManager gameManager = SSL.getInstance().getGameManager();
        InGameProfile diedProfile = gameManager.getProfile(died);

        diedProfile.setLives(diedProfile.getLives() - 1);
        diedProfile.setDeaths(diedProfile.getDeaths() + 1);

        Kit diedKit = diedProfile.getKit();
        diedKit.destroy();
        diedKit.getDeathNoise().playForAll(died.getLocation());

        DamageManager damageManager = SSL.getInstance().getDamageManager();
        Attribute killingAttribute = directKillingAttribute;

        if (killingAttribute == null && preferAttributeDamage) {
            killingAttribute = damageManager.getLastDamagingAttribute(died);
        }

        String deathMessage;
        Location tpLocation;

        Location waitLocation = SSL.getInstance().getArenaManager().getArena().getWaitLocation();
        String diedName = SSL.getInstance().getTeamManager().getPlayerColor(died) + died.getName();

        if (killingAttribute == null) {
            deathMessage = String.format("%s &7died.", diedName);
            tpLocation = waitLocation;

        } else {
            Player killer = killingAttribute.getPlayer();

            killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 2, 2);
            killer.playSound(killer.getLocation(), Sound.WOLF_HOWL, 3, 2);

            String killerName = SSL.getInstance().getTeamManager().getPlayerColor(killer) + killer.getName();

            if (killingAttribute instanceof Nameable) {
                String killName = ((Nameable) killingAttribute).getDisplayName();
                deathMessage = String.format("%s &7killed by %s &7with %s.", diedName, killerName, killName);

            } else {
                deathMessage = String.format("%s &7was killed by %s.", diedName, killerName);
            }

            if (killer == died) {
                tpLocation = waitLocation;

            } else {
                tpLocation = killer.getLocation();

                InGameProfile killerProfile = gameManager.getProfile(killer);
                killerProfile.setKills(killerProfile.getKills() + 1);
            }
        }

        Chat.DEATH.broadcast(deathMessage);

        if (teleportPlayer) {
            died.teleport(tpLocation);
            died.setVelocity(new Vector(0, 0, 0));
            died.playSound(died.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
        }

        damageManager.destroyIndicator(died);
        damageManager.removeDamageSource(died);
        damageManager.clearImmunities(died);
        damageManager.removeComboIndicator(died);

        died.setGameMode(GameMode.SPECTATOR);

        if (diedProfile.getLives() <= 0) {
            died.playSound(died.getLocation(), Sound.WITHER_DEATH, 2, 1);
            String title = MessageUtils.color("&7You have been");
            TitleAPI.sendTitle(died, title, MessageUtils.color("&celiminated!"), 7, 25, 7);
            Chat.DEATH.broadcast(MessageUtils.color(String.format("%s &7has been &celiminated!", diedName)));

            TeamManager teamManager = SSL.getInstance().getTeamManager();
            Team diedTeam = teamManager.getPlayerTeam(died);

            if (!diedTeam.isAlive()) {
                diedTeam.setLifespan(gameManager.getTicksActive());

                if (teamManager.isGameTieOrWin()) {
                    gameManager.advanceState();

                } else {
                    gameManager.addSpectator(died);
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
