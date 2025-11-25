package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.util.message.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecCommand implements CommandExecutor {

  @Override
  public boolean onCommand(
      CommandSender commandSender, Command command, String s, String[] strings
  ) {
    if (!(commandSender instanceof Player)) return false;

    GameManager gameManager = SSL.getInstance().getGameManager();

    if (!gameManager.getState().allowsSpecCommand()) {
      Chat.COMMAND.send(commandSender, "&7You can't use the /spec command now.");
      return true;
    }

    Player player = (Player) commandSender;

    if (gameManager.willSpectate(player)) {
      gameManager.removeFutureSpectator(player);
      Chat.GAME.send(player, "&7You will &aparticipate &7next game.");
    }
    else {
      gameManager.addFutureSpectator(player);
      SSL.getInstance().getArenaManager().removeVote(player);
      SSL.getInstance().getTeamManager().removeEntityFromTeam(player);
      Chat.GAME.send(player, "&7You will &cspectate &7next game.");
    }

    return true;
  }
}
