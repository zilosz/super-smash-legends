package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.List;

public class GameScoreboard implements AssembleAdapter {

    @Override
    public String getTitle(Player player) {
        return MessageUtils.color(SSL.getInstance().getResources().getConfig().getString("Scoreboard.Title"));
    }

    @Override
    public List<String> getLines(Player player) {
        return SSL.getInstance().getGameManager().getState().getScoreboard(player);
    }
}
