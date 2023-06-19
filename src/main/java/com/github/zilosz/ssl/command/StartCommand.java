package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.game.state.TutorialState;
import com.github.zilosz.ssl.SSL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {
    private final SSL plugin;

    public StartCommand(SSL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        plugin.getGameManager().skipToState(new TutorialState(plugin));
        return true;
    }
}
