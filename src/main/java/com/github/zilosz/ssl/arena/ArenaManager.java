package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.CollectionUtils;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArenaManager {
    private final SSL plugin;
    private List<Arena> arenas;
    @Getter private Arena arena;

    public ArenaManager(SSL plugin) {
        this.plugin = plugin;
    }

    public void setupArenas() {
        arenas = plugin.getResources().loadArenas();
    }

    public List<Arena> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

    public Optional<Arena> getChosenArena(Player player) {
        return arenas.stream().filter(arena -> arena.isVotedFor(player)).findAny();
    }

    public void setupArena() {
        List<Arena> bestArenas = CollectionUtils.findByHighestInt(arenas, Arena::getTotalVotes);
        arena = bestArenas.size() == 1 ? bestArenas.get(0) : CollectionUtils.selectRandom(bestArenas);
        arena.create();
    }

    public void wipePlayer(Player player) {
        arenas.forEach(arena -> arena.wipeVote(player));
    }
}
