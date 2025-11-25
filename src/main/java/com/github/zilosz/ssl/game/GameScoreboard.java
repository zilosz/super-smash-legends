package com.github.zilosz.ssl.game;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.thatkawaiisam.assemble.AssembleAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class GameScoreboard implements AssembleAdapter {

  public static String getLine() {
    return "&5&l" + StringUtils.repeat("-", getConfig().getInt("Width"));
  }

  private static Section getConfig() {
    return SSL.getInstance().getResources().getConfig().getSection("Game.Scoreboard");
  }

  @Override
  public String getTitle(Player player) {
    return MessageUtils.color(getConfig().getString("Title"));
  }

  @Override
  public List<String> getLines(Player player) {
    return SSL.getInstance().getGameManager().getState().getScoreboard(player);
  }
}
