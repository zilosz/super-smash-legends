package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.file.FileUtility;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import com.github.zilosz.ssl.utils.world.CustomWorldType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Arena {
    private final Section config;
    private final Set<Player> playersWithVotes = new HashSet<>();

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

    void addVote(Player player) {
        this.playersWithVotes.add(player);
    }

    void wipeVote(Player player) {
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
        String path = FileUtility.buildPath("arenas", this.config.getString("SchematicName"));
        File schematic = FileUtility.loadSchematic(SSL.getInstance(), path);
        SSL.getInstance().getWorldManager().createWorld(CustomWorldType.ARENA, schematic, pasteVector);
    }

    public Location getWaitLocation() {
        return YamlReader.location(CustomWorldType.ARENA.getWorldName(), this.config.getString("WaitLocation"));
    }

    public List<Location> getTutorialLocations() {
        return this.getLocations("TutorialLocations");
    }

    private List<Location> getLocations(String path) {
        return YamlReader.locations(CustomWorldType.ARENA.getWorldName(), this.config.getStringList(path));
    }

    public List<Location> getSpawnLocations() {
        return this.getLocations("SpawnLocations");
    }
}
