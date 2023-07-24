package com.github.zilosz.ssl.command.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.state.GameStateType;
import com.github.zilosz.ssl.utils.message.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkipCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 1) return false;

        String name = strings[0].toUpperCase();

        try {
            SSL.getInstance().getGameManager().skipToState(GameStateType.valueOf(name));

        } catch (IllegalArgumentException e) {
            Chat.COMMAND.send(commandSender, String.format("&f%s &7is not a valid state.", name));
        }

        return true;
    }
}
