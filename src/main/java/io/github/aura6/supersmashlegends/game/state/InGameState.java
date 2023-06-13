package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.connorlinfoot.titleapi.TitleAPI;
import com.nametagedit.plugin.NametagEdit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.attribute.Nameable;
import io.github.aura6.supersmashlegends.damage.DamageManager;
import io.github.aura6.supersmashlegends.game.InGameProfile;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.Team;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.HotbarItem;
import io.github.aura6.supersmashlegends.utils.NmsUtils;
import io.github.aura6.supersmashlegends.utils.effect.DeathNPC;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
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
import java.util.concurrent.atomic.AtomicReference;
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

    public InGameState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "InGame";
    }

    @Override
    public boolean isInArena() {
        return true;
    }

    @Override
    public boolean allowKitSelection() {
        return this.plugin.getResources().getConfig().getBoolean("Game.AllowKitSelectionInGame");
    }

    @Override
    public boolean updatesKitSkins() {
        return true;
    }

    private String getPlayerLivesText(Player player, int lifeCap, String nameColor) {
        int lives = plugin.getGameManager().getProfile(player).getLives();
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
    public List<String> getScoreboard(Player player) {

        List<String> scoreboard = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&f&lMinutes Left",
                "&e{MIN_LEFT}",
                ""
        ));

        int playerIndex = scoreboard.size();

        Replacers replacers = new Replacers().add("MIN_LEFT", MessageUtils.secToMin(this.secLeft));

        if (this.plugin.getGameManager().isPlayerAlive(player)) {
            scoreboard.add("");
            scoreboard.add("&f&lKit");
            scoreboard.add("{KIT}");

            try {
                replacers.add("KIT", this.plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
            } catch (NullPointerException ignored) {}
        }

        scoreboard.add("&5&l---------------------");

        TeamManager teamManager = this.plugin.getTeamManager();
        int lifeCap = this.plugin.getResources().getConfig().getInt("Game.Lives");
        Set<Player> alivePlayers = this.plugin.getGameManager().getAlivePlayers();

        if (teamManager.getTeamSize() == 1) {
            scoreboard.add(playerIndex, "&f&lPlayers");

            if (scoreboard.size() + alivePlayers.size() <= MAX_SCOREBOARD_SIZE) {

                for (Player alivePlayer : alivePlayers) {
                    scoreboard.add(playerIndex + 1, getPlayerLivesText(alivePlayer, lifeCap, "&7"));
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

                        if (this.plugin.getGameManager().isPlayerAlive(p)) {
                            scoreboard.add(playerIndex + 1, getPlayerLivesText(p, lifeCap, team.getColor()));
                        }
                    }
                }

            } else {
                scoreboard.add(playerIndex + 1, "&e&l" + aliveTeams.size() + " &7teams alive.");
            }
        }

        return replacers.replaceLines(scoreboard);
    }

    private void giveTracker(Player player) {

        this.trackerItems.put(player, this.plugin.getResources().giveHotbarItem("PlayerTracker", player, e -> {

            Optional.ofNullable(this.closestTargets.get(player)).ifPresentOrElse(target -> {
                String distance = FORMAT.format(EntityUtils.getDistance(player, target));
                String name = this.plugin.getTeamManager().getPlayerColor(target) + target.getName();
                Chat.TRACKER.send(player, String.format("%s &7is &e%s &7blocks away.", name, distance));
            }, () -> {
                Chat.TRACKER.send(player, "&7There are no players to track.");
            });
        }));

        this.trackerTasks.put(player, Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (!player.getWorld().getName().equals("arena")) return;

            List<Player> players = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getWorld().getName().equals("arena"))
                    .filter(p -> this.plugin.getGameManager().isPlayerAlive(p))
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

                String name =  this.plugin.getTeamManager().getPlayerColor(closest) + closest.getName();
                String distance = FORMAT.format(EntityUtils.getDistance(player, closest));
                actionBar = String.format("%s &7is &e%s &7blocks away.", name, distance);

                player.setCompassTarget(closest.getLocation());
            }

            if (player.getInventory().getHeldItemSlot() == this.trackerItems.get(player).getSlot()) {
                ActionBarAPI.sendActionBar(player, MessageUtils.color(actionBar));
            }
        }, 0, 5));
    }

    private void removeTracker(Player player) {
        Optional.ofNullable(this.trackerItems.remove(player)).ifPresent(HotbarItem::destroy);
        Optional.ofNullable(this.trackerTasks.remove(player)).ifPresent(BukkitTask::cancel);
        this.closestTargets.remove(player);
    }

    @Override
    public void start() {
        Section timerConfig = this.plugin.getResources().getConfig().getSection("Game.Timer");
        int extraPlayers = Math.max(0, this.plugin.getGameManager().getAlivePlayers().size() - 4);
        this.secLeft = timerConfig.getInt("DefaultSeconds") + extraPlayers * timerConfig.getInt("SecondsPerExtraPlayer");

        this.gameTimer = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (--this.secLeft <= 0) {
                Chat.GAME.broadcast("&7Ran out of time!");
                this.plugin.getGameManager().advanceState();
            }
        }, 20, 20);

        List<Location> spawnLocations = this.plugin.getArenaManager().getArena().getSpawnLocations();
        List<Location> spawnsLeft = new ArrayList<>(spawnLocations);
        Comparator<Location> comparator = Comparator.comparingDouble(Arena::getTotalDistanceToPlayers);

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (this.plugin.getGameManager().isPlayerAlive(player)) {
                Location spawn = Collections.max(spawnsLeft, comparator);
                spawnsLeft.remove(spawn);
                player.teleport(spawn);

                if (spawnsLeft.isEmpty()) {
                    spawnsLeft = spawnLocations;
                }

                this.plugin.getGameManager().getProfile(player).getKit().activate();

                if (this.plugin.getTeamManager().getTeamSize() > 1) {
                    String color = this.plugin.getTeamManager().getPlayerColor(player);
                    NametagEdit.getApi().setPrefix(player, MessageUtils.color(color));
                }

                this.giveTracker(player);
            }

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 0.5f);
            TitleAPI.sendTitle(player, MessageUtils.color("&7The &5game &7has started!"), "", 5, 30, 5);
        }
    }

    private void respawnPlayer(Player player) {
        if (!this.plugin.getGameManager().isPlayerAlive(player)) return;

        player.teleport(this.plugin.getArenaManager().getArena().getFarthestSpawnFromPlayers());

        TitleAPI.sendTitle(player, MessageUtils.color("&7You have &arespawned&7."), MessageUtils.color("&cAvenge &7your death!"), 10, 30, 10);
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 2);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0.8f);

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);

        Kit kit = this.plugin.getKitManager().getSelectedKit(player);
        kit.giveItems();
        kit.activate();
    }

    @Override
    public void end() {
        this.gameTimer.cancel();

        this.skinRestorers.forEach(BukkitTask::cancel);
        this.skinRestorers.clear();

        this.respawnTasks.forEach((player, respawnTask) -> {
            this.respawnPlayer(player);
            respawnTask.cancel();
        });

        this.respawnTasks.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            this.removeTracker(player);
            TitleAPI.clearTitle(player);

            if (this.plugin.getGameManager().isPlayerAlive(player)) {
                this.plugin.getKitManager().getSelectedKit(player).deactivate();
            }
        }
    }

    private void handleDeath(Player died, boolean makeNpc) {
        this.plugin.getKitManager().getSelectedKit(died).destroy();

        if (makeNpc) {
            DeathNPC.spawn(plugin, died);
        }

        died.setVelocity(new Vector(0, 0, 0));
        died.setGameMode(GameMode.SPECTATOR);
        NmsUtils.getConnection(died).a(new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN));

        InGameProfile profile = plugin.getGameManager().getProfile(died);
        profile.setLives(profile.getLives() - 1);
        profile.setDeaths(profile.getDeaths() + 1);

        DamageManager damageManager = plugin.getDamageManager();

        final AtomicReference<String> deathMessage = new AtomicReference<>();
        final AtomicReference<Location> tpLocation = new AtomicReference<>();

        String diedName = this.plugin.getTeamManager().getPlayerColor(died) + died.getName();

        damageManager.getLastDamagingAttribute(died).ifPresentOrElse(attribute -> {
            Player killer = attribute.getPlayer();

            killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 2, 2);
            killer.playSound(killer.getLocation(), Sound.WOLF_HOWL, 3, 2);

            InGameProfile killerProfile = plugin.getGameManager().getProfile(killer);
            killerProfile.setKills(killerProfile.getKills() + 1);

            String killerName = this.plugin.getTeamManager().getPlayerColor(killer) + killer.getName();

            if (attribute instanceof Nameable) {
                String killName = ((Nameable) attribute).getDisplayName();
                deathMessage.set(String.format("%s &7killed by %s &7with %s.", diedName, killerName, killName));

            } else {
                deathMessage.set(String.format("%s &7was killed by %s.", diedName, killerName));
            }

            tpLocation.set(killer.getLocation());
        }, () -> {
            tpLocation.set(plugin.getArenaManager().getArena().getWaitLocation());
            deathMessage.set(String.format("%s &7died.", diedName));
        });

        Chat.DEATH.broadcast(deathMessage.get());
        died.teleport(tpLocation.get());

        damageManager.destroyIndicator(died);
        damageManager.removeDamageSource(died);
        damageManager.clearImmunities(died);

        if (profile.getLives() <= 0) {
            died.playSound(died.getLocation(), Sound.WITHER_DEATH, 2, 1);
            TitleAPI.sendTitle(died, MessageUtils.color("&7You have been"), MessageUtils.color("&celiminated!"), 7, 25, 7);
            Chat.DEATH.broadcast(MessageUtils.color(String.format("%s &7has been &celiminated!", diedName)));

            Team diedTeam = plugin.getTeamManager().getPlayerTeam(died);

            if (!diedTeam.isAlive()) {
                diedTeam.setLifespan(plugin.getGameManager().getTicksActive());
            }

            if (plugin.getTeamManager().isGameTieOrWin()) {
                plugin.getGameManager().advanceState();
            }

            return;
        }

        TitleAPI.sendTitle(died, MessageUtils.color("&7You &cdied!"), MessageUtils.color("&7Respawning soon..."), 7, 25, 7);
        died.playSound(died.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 1);

        respawnTasks.put(died, new BukkitRunnable() {
            int secondsLeft = plugin.getResources().getConfig().getInt("Game.DeathWaitSeconds");
            float pitch = 0.5f;
            final double pitchStep = 1.5 / secondsLeft;

            @Override
            public void run() {

                if (secondsLeft == 0) {
                    respawnPlayer(died);
                    respawnTasks.remove(died).cancel();
                    return;
                }

                String title = MessageUtils.color("&7Respawning in...");
                TitleAPI.sendTitle(died, title, MessageUtils.color("&5&l" + secondsLeft), 4, 12, 4);
                died.playSound(died.getLocation(), Sound.ENDERDRAGON_HIT, 2, pitch);

                pitch += pitchStep;
                secondsLeft--;
            }

        }.runTaskTimer(plugin, 60, 20));
    }

    private void registerDamageTaken(Player player, double damage) {
        InGameProfile profile = plugin.getGameManager().getProfile(player);
        profile.setDamageTaken(profile.getDamageTaken() + damage);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRegularDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (!this.plugin.getGameManager().isPlayerAlive(player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {

            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.teleport(this.plugin.getArenaManager().getArena().getWaitLocation());

            } else if (player.getHealth() - event.getFinalDamage() > 0) {
                event.setDamage(0);
                this.handleDeath(player, false);
                this.registerDamageTaken(player, player.getHealth());
            }

        } else if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            this.registerDamageTaken(player, event.getFinalDamage());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        event.setDeathMessage("");
        this.handleDeath(event.getEntity(), true);
    }

    @EventHandler
    public void onQuitInGame(PlayerQuitEvent event) {
        this.removeTracker(event.getPlayer());
    }
}
