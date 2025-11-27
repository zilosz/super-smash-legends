package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.file.FileUtility;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.MessageUtils;
import com.github.zilosz.ssl.util.world.CustomWorldType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Arena {
  private final Section config;
  private final Collection<Player> playersWithVotes = new HashSet<>();

  public Arena(Section config) {
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

  void addVote(Player player) {
    playersWithVotes.add(player);
  }

  void wipeVote(Player player) {
    playersWithVotes.remove(player);
  }

  public int getTotalVotes() {
    return playersWithVotes.size();
  }

  public boolean isVotedBy(Player player) {
    return playersWithVotes.contains(player);
  }

  public void create() {
    Vector pasteVector = YamlReader.vector(config.getString("PasteVector"));
    String path = FileUtility.buildPath("arenas", config.getString("SchematicName"));
    File schem = FileUtility.loadSchematic(SSL.getInstance(), path);
    SSL.getInstance().getWorldManager().createWorld(CustomWorldType.ARENA, schem, pasteVector);
  }

  public Location getWaitLocation() {
    return YamlReader.location(CustomWorldType.ARENA.getWorldName(),
        config.getString("WaitLocation")
    );
  }

  public List<Location> getTutorialLocations() {
    return getLocations("TutorialLocations");
  }

  private List<Location> getLocations(String path) {
    return YamlReader.locations(CustomWorldType.ARENA.getWorldName(), config.getStringList(path));
  }

  public List<Location> getSpawnLocations() {
    return getLocations("SpawnLocations");
  }
}
