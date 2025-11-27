package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArenaManager {
  private List<Arena> arenas;
  private Map<Player, Arena> playerVotes;
  @Getter private Arena arena;

  public void setupArenas() {
    Section config = SSL.getInstance().getResources().getArenas();

    arenas = YamlReader
        .sections(config)
        .stream()
        .map(Arena::new)
        .sorted(Comparator.comparing(Arena::getName))
        .collect(Collectors.toList());

    playerVotes = new HashMap<>();
  }

  public List<Arena> getArenas() {
    return Collections.unmodifiableList(arenas);
  }

  public void setupArena() {
    List<Arena> bestArenas = CollectionUtils.maxElementByInt(arenas, Arena::getTotalVotes);
    arena = CollectionUtils.randChoice(bestArenas);
    arena.create();
  }

  public void addVote(Player player, Arena arena) {
    arena.addVote(player);
    playerVotes.put(player, arena);
  }

  public void removeVote(Player player) {
    Optional.ofNullable(playerVotes.remove(player)).ifPresent(arena -> arena.wipeVote(player));
  }

  public Arena getChosenArena(Player player) {
    return playerVotes.get(player);
  }
}
