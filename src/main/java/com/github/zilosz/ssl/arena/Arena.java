package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.file.FileUtility;
import com.github.zilosz.ssl.utils.file.PathBuilder;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.Bukkit;
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
    private final SSL plugin;
    private final Section config;
    private final List<UUID> playerVotes = new ArrayList<>();

    public Arena(SSL plugin, Section config) {
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

    public static double getTotalDistanceToPlayers(Location location) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> SSL.getInstance().getGameManager().isPlayerAlive(player))
                .mapToDouble(player -> player.getLocation().distanceSquared(location)).sum();
    }

    public Location getFarthestSpawnFromPlayers() {
        return Collections.max(this.getSpawnLocations(), Comparator.comparingDouble(Arena::getTotalDistanceToPlayers));
    }
}
