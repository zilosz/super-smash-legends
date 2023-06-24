package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.CollectionUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArenaManager {
    private final List<Arena> arenas = new ArrayList<>();
    @Getter private Arena arena;

    public void setupArenas() {
        this.arenas.clear();

        Section arenaConfig = SSL.getInstance().getResources().getArenas();
        arenaConfig.getKeys().forEach(key -> this.arenas.add(new Arena(arenaConfig.getSection((String) key))));
    }

    public List<Arena> getArenas() {
        return Collections.unmodifiableList(this.arenas);
    }

    public Optional<Arena> getChosenArena(Player player) {
        return this.arenas.stream().filter(arena -> arena.isVotedBy(player)).findAny();
    }

    public void setupArena() {
        List<Arena> bestArenas = CollectionUtils.findByHighestInt(this.arenas, Arena::getTotalVotes);
        this.arena = bestArenas.size() == 1 ? bestArenas.get(0) : CollectionUtils.selectRandom(bestArenas);
        this.arena.create();
    }

    public void wipePlayer(Player player) {
        this.arenas.forEach(arena -> arena.wipeVote(player));
    }
}
