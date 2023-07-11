package com.github.zilosz.ssl.arena;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArenaManager {
    private List<Arena> arenas;
    @Getter private Arena arena;

    public void setupArenas() {
        Section config = SSL.getInstance().getResources().getArenas();

        this.arenas = YamlReader.sections(config).stream()
                .map(Arena::new)
                .sorted(Comparator.comparing(Arena::getName))
                .collect(Collectors.toList());
    }

    public List<Arena> getArenas() {
        return Collections.unmodifiableList(this.arenas);
    }

    public Optional<Arena> getChosenArena(Player player) {
        return this.arenas.stream().filter(arena -> arena.isVotedBy(player)).findAny();
    }

    public void setupArena() {
        List<Arena> bestArenas = CollectionUtils.findByHighestInt(this.arenas, Arena::getTotalVotes);
        this.arena = CollectionUtils.selectRandom(bestArenas);
        this.arena.create();
    }

    public void wipePlayer(Player player) {
        this.arenas.forEach(arena -> arena.wipeVote(player));
    }
}
