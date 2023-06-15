package io.github.aura6.supersmashlegends.game;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.List;

public class GameScoreboard implements AssembleAdapter {
    private static final SuperSmashLegends plugin = SuperSmashLegends.getInstance();

    @Override
    public String getTitle(Player player) {
        return MessageUtils.color(plugin.getResources().getConfig().getString("Scoreboard.Title"));
    }

    @Override
    public List<String> getLines(Player player) {
        return plugin.getGameManager().getState().getScoreboard(player);
    }
}
