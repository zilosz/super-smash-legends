package com.github.zilosz.ssl.command.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.state.GameStateType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlayCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        SSL.getInstance().getGameManager().skipToState(GameStateType.IN_GAME);
        return true;
    }
}
