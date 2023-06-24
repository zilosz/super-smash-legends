package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.file.FileUtility;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
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

public class Arena {
    private final Section config;
    private final List<Player> playersWithVotes = new ArrayList<>();

    public Arena(Section config) {
        this.config = config;
    }

    public String getName() {
        return MessageUtils.color(this.config.getString("Name"));
    }

    public String getAuthors() {
        return this.config.getString("Authors");
    }

    public ItemStack getItemStack() {
        return YamlReader.stack(this.config.getSection("Item"));
    }

    public void addVote(Player player) {
        this.playersWithVotes.add(player);
    }

    public void wipeVote(Player player) {
        this.playersWithVotes.remove(player);
    }

    public int getTotalVotes() {
        return this.playersWithVotes.size();
    }

    public boolean isVotedBy(Player player) {
        return this.playersWithVotes.contains(player);
    }

    public void create() {
        Vector pasteVector = YamlReader.vector(this.config.getString("PasteVector"));
        String path = FileUtility.buildPath("arena", this.config.getString("SchematicName"));
        File schematic = FileUtility.loadSchematic(SSL.getInstance(), path);
        SSL.getInstance().getWorldManager().createWorld("arena", schematic, pasteVector);
    }

    public Location getWaitLocation() {
        return YamlReader.location("arena", this.config.getString("WaitLocation"));
    }

    public List<Location> getTutorialLocations() {
        return YamlReader.locations("arena", this.config.getStringList("TutorialLocations"));
    }

    public Location getFarthestSpawnFromPlayers() {
        return Collections.max(this.getSpawnLocations(), Comparator.comparingDouble(Arena::getTotalDistanceToPlayers));
    }

    public List<Location> getSpawnLocations() {
        return YamlReader.locations("arena", this.config.getStringList("SpawnLocations"));
    }

    public static double getTotalDistanceToPlayers(Location location) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> SSL.getInstance().getGameManager().isPlayerAlive(player))
                .mapToDouble(player -> player.getLocation().distanceSquared(location))
                .sum();
    }
}
