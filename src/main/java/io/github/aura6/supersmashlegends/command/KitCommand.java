package io.github.aura6.supersmashlegends.command;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.kit.KitSelector;
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

        if (this.plugin.getGameManager().getState().allowKitSelection()) {

            if (strings.length > 1) {
                return false;
            }

            if (strings.length == 0) {
                new KitSelector().build().open(player);

            } else  {
                KitManager kitManager = this.plugin.getKitManager();
                String name = StringUtils.capitalize(strings[0].toLowerCase());

                kitManager.getKitByName(name).ifPresentOrElse(
                        kit -> kitManager.setKit(player, kit),
                        () -> Chat.KIT.send(player, String.format("\"%s\" &7is not a valid kit.", name)));
            }

        } else {
            Chat.KIT.send(player, "&7You cannot change your kit at this time.");
        }

        return true;
    }
}
