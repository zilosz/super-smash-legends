package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.state.EndState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        SSL.getInstance().getGameManager().skipToState(new EndState());
        return true;
    }
}
