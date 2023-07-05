package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.state.GameStateType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        SSL.getInstance().getGameManager().skipToState(GameStateType.END);
        return true;
    }
}
