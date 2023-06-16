package io.github.aura6.supersmashlegends.command;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.GameManager;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        GameManager gameManager = SuperSmashLegends.getInstance().getGameManager();

        if (!gameManager.getState().allowSpecCommand()) {
            Chat.COMMAND.send(commandSender, "&7You can't use the /spec command now.");
            return true;
        }

        Player player = (Player) commandSender;

        if (gameManager.willSpectate(player)) {
            gameManager.removeFutureSpectator(player);
            Chat.GAME.send(player, "&7You will &5participate &7next game.");

        } else {
            gameManager.addFutureSpectator(player);
            SuperSmashLegends.getInstance().getArenaManager().wipePlayer(player);
            SuperSmashLegends.getInstance().getTeamManager().wipePlayer(player);
            Chat.GAME.send(player, "&7You will &5spectate &7next game.");
        }

        return true;
    }
}
