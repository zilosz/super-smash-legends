package io.github.aura6.supersmashlegends.game.state;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.arena.Arena;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TutorialState extends GameState {
    private Set<Player> playersInTutorial;
    private final List<BukkitTask> movers = new ArrayList<>();
    private final List<BukkitTask> moveDelayers = new ArrayList<>();
    private final List<BukkitTask> tutorialSchedulers = new ArrayList<>();
    private final List<BukkitTask> ruleDisplayers = new ArrayList<>();

    public TutorialState(SuperSmashLegends plugin) {
        super(plugin);
    }

    @Override
    public String getConfigName() {
        return "Tutorial";
    }

    @Override
    public List<String> getScoreboard(Player player) {
        Arena arena = plugin.getArenaManager().getArena();

        Replacers replacers = new Replacers()
                .add("ARENA", arena.getName())
                .add("AUTHORS", arena.getAuthors());

        List<String> lore = new ArrayList<>(Arrays.asList(
                "&5&l---------------------",
                "&7Enjoy the tutorial!",
                "",
                "&fArena: {ARENA}",
                "&fAuthors: &7{AUTHORS}",
                "",
                "&5&l---------------------"
        ));

        if (!plugin.getGameManager().isSpectator(player)) {
            lore.add(6, "&fKit: &5{KIT}");
            replacers.add("KIT", plugin.getKitManager().getSelectedKit(player).getBoldedDisplayName());
        }

        return replacers.replaceLines(lore);
    }

    @Override
    public boolean isNotInGame() {
        return false;
    }

    private void stopPlayer(Player player) {
        player.setVelocity(new Vector(0, 0, 0));
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlySpeed(0.1f);
    }

    private void startTutorialMovement(Player player, List<Location> points, double speed, int from, int to) {

        if (from == points.size() || points.size() <= 1) {
            playersInTutorial.remove(player);
            stopPlayer(player);

            if (playersInTutorial.size() == 0) {
                plugin.getGameManager().advanceState();
            }

            return;
        }

        Vector velocity = VectorUtils.fromTo(points.get(from), points.get(to)).normalize().multiply(speed);
        BukkitTask moveTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> player.setVelocity(velocity), 0, 0);
        movers.add(moveTask);

        int stepDuration = (int) ((points.get(from).distance(points.get(to))) / speed);

        moveDelayers.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
            moveTask.cancel();
            startTutorialMovement(player, points, speed, from + 1, (to + 1) % points.size());
        }, stepDuration));
    }

    @Override
    public void start() {
        Set<Player> players = plugin.getGameManager().getAlivePlayers();
        playersInTutorial = new HashSet<>(players);

        Arena arena = plugin.getArenaManager().getArena();
        List<Location> tutorialLocations = arena.getTutorialLocations();

        double totalDistance = tutorialLocations.get(0).distance(tutorialLocations.get(tutorialLocations.size() - 1));

        for (int i = 1; i < tutorialLocations.size(); i++) {
            totalDistance += tutorialLocations.get(i).distance(tutorialLocations.get(i - 1));
        }

        totalDistance += tutorialLocations.get(tutorialLocations.size() - 1).distance(tutorialLocations.get(0));

        int tutorialDuration = plugin.getResources().getConfig().getInt("Game.Tutorial.DurationTicks");
        double velocity = totalDistance / tutorialDuration;
        int delay = plugin.getResources().getConfig().getInt("Game.Tutorial.DelayTicks");

        List<String> rules = new Replacers()
                .add("LIVES", String.valueOf(plugin.getResources().getConfig().getInt("Game.Lives")))
                .replaceLines(plugin.getResources().getConfig().getStringList("Rules"));

        for (Player player : players) {
            player.teleport(tutorialLocations.get(0));

            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 1);
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlySpeed(0);

            tutorialSchedulers.add(Bukkit.getScheduler().runTaskLater(plugin,
                    () -> startTutorialMovement(player, tutorialLocations, velocity, 0, 1), delay));

            int ruleDelay = ((int) (double) tutorialDuration / (rules.size() + 1));

            ruleDisplayers.add(new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {

                    if (i == rules.size()) {
                        cancel();
                        return;
                    }

                    ActionBarAPI.sendActionBar(player, rules.get(i++));
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.5F);
                }

            }.runTaskTimer(plugin, ruleDelay + 15, ruleDelay));
        }
    }

    @Override
    public void end() {
        movers.forEach(BukkitTask::cancel);
        movers.clear();

        moveDelayers.forEach(BukkitTask::cancel);
        moveDelayers.clear();

        tutorialSchedulers.forEach(BukkitTask::cancel);
        tutorialSchedulers.clear();

        ruleDisplayers.forEach(BukkitTask::cancel);
        ruleDisplayers.clear();

        playersInTutorial.forEach(this::stopPlayer);
        playersInTutorial.clear();
    }
}
