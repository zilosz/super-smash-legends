package io.github.aura6.supersmashlegends.command;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {
    private final SuperSmashLegends plugin;

    public StartCommand(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        plugin.getStateManager().startGame();
        return false;
    }
}
