package io.github.aura6.supersmashlegends.command;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.state.EndState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndCommand implements CommandExecutor {
    private final SuperSmashLegends plugin;

    public EndCommand(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        plugin.getGameManager().skipToState(new EndState(plugin));
        return false;
    }
}
