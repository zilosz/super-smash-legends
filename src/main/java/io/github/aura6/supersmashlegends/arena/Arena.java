package io.github.aura6.supersmashlegends.arena;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.event.PlayerFinishTutorialEvent;
import io.github.aura6.supersmashlegends.utils.WorldMaker;
import io.github.aura6.supersmashlegends.utils.file.FileLoader;
import io.github.aura6.supersmashlegends.utils.file.PathBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.aura6.supersmashlegends.utils.message.Replacers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Arena {
    private final SuperSmashLegends plugin;
    private final Section config;
    private final List<UUID> playerVotes = new ArrayList<>();

    public Arena(SuperSmashLegends plugin, Section config) {
        this.plugin = plugin;
        this.config = config;
    }

    public String getName() {
        return MessageUtils.color(config.getString("Name"));
    }

    public String getAuthors() {
        return config.getString("Authors");
    }

    public ItemStack getItemStack() {
        return YamlReader.readItemStack(config.getSection("Item"));
    }

    public void addVote(Player player) {
        playerVotes.add(player.getUniqueId());
    }

    public void wipeVote(Player player) {
        playerVotes.remove(player.getUniqueId());
    }

    public int getTotalVotes() {
        return playerVotes.size();
    }

    public boolean isVotedFor(Player player) {
        return playerVotes.contains(player.getUniqueId());
    }

    public void create() {
        Vector pasteVector = YamlReader.readVector(config.getString("PasteVector"));
        String path = PathBuilder.build("arena", config.getString("SchematicName"));
        File schematic = FileLoader.loadSchematic(plugin, path);
        WorldMaker.create("arena", schematic, pasteVector);
    }

    public void teleportToWait(Player player) {
        player.teleport(YamlReader.readLocation("arena", config.getString("WaitLocation")));
    }

    private void startTutorialMovement(Player player, List<Location> points, double speed, int from, int to) {

        if (from == points.size() || points.size() <= 1) {
            player.setVelocity(new Vector(0, 0, 0));
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlySpeed(0.1f);

            PlayerFinishTutorialEvent event = new PlayerFinishTutorialEvent();
            Bukkit.getPluginManager().callEvent(event);

            return;
        }

        Vector velocity = VectorUtils.fromTo(points.get(from), points.get(to)).normalize().multiply(speed);
        BukkitTask moveTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> player.setVelocity(velocity), 0, 0);

        int stepDuration = (int) ((points.get(from).distance(points.get(to))) / speed);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(from + ", " + to);
            moveTask.cancel();
            startTutorialMovement(player, points, speed, from + 1, (to + 1) % points.size());
        }, stepDuration);
    }

    public void startTutorial(Player player) {
        List<Location> tutorialLocations = YamlReader.readLocations("arena", config.getStringList("TutorialLocations"));
        player.teleport(tutorialLocations.get(0));

        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(0);

        double totalDistance = tutorialLocations.get(0).distance(tutorialLocations.get(tutorialLocations.size() - 1));

        for (int i = 1; i < tutorialLocations.size(); i++) {
            totalDistance += tutorialLocations.get(i).distance(tutorialLocations.get(i - 1));
        }

        totalDistance += tutorialLocations.get(tutorialLocations.size() - 1).distance(tutorialLocations.get(0));

        int tutorialDuration = plugin.getResources().getConfig().getInt("Game.Tutorial.DurationTicks");
        double velocity = totalDistance / tutorialDuration;
        int delay = plugin.getResources().getConfig().getInt("Game.Tutorial.DelayTicks");

        Bukkit.getScheduler().runTaskLater(plugin, () -> startTutorialMovement(player, tutorialLocations, velocity, 0, 1), delay);

        List<String> rules = new Replacers()
                .add("LIVES", String.valueOf(plugin.getResources().getConfig().getInt("Game.Lives")))
                .replaceLines(plugin.getResources().getConfig().getStringList("Rules"));

        int ruleDelay = ((int) (double) tutorialDuration / (rules.size() + 1));

        new BukkitRunnable() {
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

        }.runTaskTimer(plugin, ruleDelay, ruleDelay);
    }

    public List<Location> getSpawnLocations() {
        return YamlReader.readLocations("arena", config.getStringList("SpawnLocations"));
    }

    public void teleportAllOnStart() {
        List<Location> spawnsLeft = getSpawnLocations();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location spawn = MathUtils.selectRandom(spawnsLeft);
            spawnsLeft.remove(spawn);
            player.teleport(spawn);

            if (spawnsLeft.size() == 0) {
                spawnsLeft = getSpawnLocations();
            }
        }
    }
}
