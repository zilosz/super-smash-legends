package io.github.aura6.supersmashlegends.game;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.state.GameState;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.List;

public class GameScoreboard implements AssembleAdapter {
    private final SuperSmashLegends plugin;

    public GameScoreboard(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getTitle(Player player) {
        return "&5&lMytheral &fNetwork";
    }

    @Override
    public List<String> getLines(Player player) {
        GameState state = plugin.getStateManager().getState();
        List<String> lines = plugin.getResources().getScoreboard().getStringList("Contents." + state.getConfigName());
        return state.getScoreboardReplacers(player).replaceLines(lines);
    }
}
