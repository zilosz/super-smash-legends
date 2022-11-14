package io.github.aura6.supersmashlegends.arena;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.file.FileUtility;
import io.github.aura6.supersmashlegends.utils.file.PathBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        return YamlReader.stack(config.getSection("Item"));
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
        Vector pasteVector = YamlReader.vector(config.getString("PasteVector"));
        String path = PathBuilder.build("arena", config.getString("SchematicName"));
        File schematic = FileUtility.loadSchematic(plugin, path);
        plugin.getWorldManager().createWorld("arena", schematic, pasteVector);
    }

    public Location getWaitLocation() {
        return YamlReader.location("arena", config.getString("WaitLocation"));
    }

    public List<Location> getTutorialLocations() {
        return YamlReader.locations("arena", config.getStringList("TutorialLocations"));
    }

    public List<Location> getSpawnLocations() {
        return YamlReader.locations("arena", config.getStringList("SpawnLocations"));
    }

    public List<Location> getPowerLocations() {
        return YamlReader.locations("arena", config.getStringList("PowerLocations"));
    }

    private double getTotalDistanceToPlayers(Location location) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                .mapToDouble(player -> player.getLocation().distanceSquared(location)).sum();
    }

    public Location getFarthestSpawnFromPlayers() {
        return Collections.max(getSpawnLocations(), Comparator.comparingDouble(this::getTotalDistanceToPlayers));
    }
}
