package io.github.aura6.supersmashlegends.game;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
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
        return plugin.getGameManager().getState().getScoreboard(player);
    }
}
