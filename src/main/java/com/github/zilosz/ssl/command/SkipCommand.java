package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkipCommand implements CommandExecutor {
    private final SSL plugin;

    public SkipCommand(SSL plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (strings.length == 1) {
            GameManager gameManager = plugin.getGameManager();
            gameManager.findState(strings[0]).ifPresent(gameManager::skipToState);
            return true;
        }

        return false;
    }
}
