package io.github.aura6.supersmashlegends.command;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.game.state.GameState;
import io.github.aura6.supersmashlegends.game.state.InGameState;
import io.github.aura6.supersmashlegends.game.state.LobbyState;
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final SuperSmashLegends plugin;

    public KitCommand(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;
        GameState state = this.plugin.getGameManager().getState();

        boolean canChangeInGame = this.plugin.getResources().getConfig().getBoolean("Game.AllowKitSelectionInGame");

        if (state instanceof LobbyState || state instanceof InGameState && canChangeInGame) {

            if (strings.length == 0) {
                this.plugin.getKitSelector().build().open(player);

            } else {
                KitManager kitManager = this.plugin.getKitManager();
                String name = StringUtils.capitalize(strings[0].toLowerCase());
                kitManager.getKitByName(name).ifPresent(kit -> kitManager.handleKitSelection(player, kit));
            }

        } else {
            Chat.KIT.send(player, "&7You cannot change your kit at this time.");
        }

        return true;
    }
}
