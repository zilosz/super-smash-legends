package io.github.aura6.supersmashlegends.command;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkipCommand implements CommandExecutor {
    private final SuperSmashLegends plugin;

    public SkipCommand(SuperSmashLegends plugin) {
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
