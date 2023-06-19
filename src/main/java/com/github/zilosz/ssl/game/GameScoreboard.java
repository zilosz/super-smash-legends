package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.List;

public class GameScoreboard implements AssembleAdapter {
    private static final SSL plugin = SSL.getInstance();

    @Override
    public String getTitle(Player player) {
        return MessageUtils.color(plugin.getResources().getConfig().getString("Scoreboard.Title"));
    }

    @Override
    public List<String> getLines(Player player) {
        return plugin.getGameManager().getState().getScoreboard(player);
    }
}
