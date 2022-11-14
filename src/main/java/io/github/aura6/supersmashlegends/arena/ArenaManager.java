package io.github.aura6.supersmashlegends.arena;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArenaManager {
    private final SuperSmashLegends plugin;
    private List<Arena> arenas;
    @Getter private Arena arena;

    public ArenaManager(SuperSmashLegends plugin) {
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
        List<Arena> bestArenas = MathUtils.findByHighestInt(arenas, Arena::getTotalVotes);
        arena = bestArenas.size() == 1 ? bestArenas.get(0) : MathUtils.selectRandom(bestArenas);
        arena.create();
    }
}
