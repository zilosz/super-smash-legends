package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.kit.KitSelector;
import com.github.zilosz.ssl.utils.CollectionUtils;
import com.github.zilosz.ssl.utils.message.Chat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {
    private final SSL plugin;

    public KitCommand(SSL plugin) {
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

            } else {
                KitManager kitManager = this.plugin.getKitManager();
                String name = StringUtils.capitalize(strings[0].toLowerCase());

                kitManager.getKitByName(name).ifPresentOrElse(kit -> {
                    kitManager.setKit(player, kit);
                }, () -> {
                    if (name.equalsIgnoreCase("random")) {
                        Chat.KIT.send(player, "&7Selecting a random kit...");
                        kitManager.setKit(player, CollectionUtils.selectRandom(kitManager.getKits()));
                    } else {
                        Chat.KIT.send(player, String.format("&7\"%s\" &7is not a valid kit.", name));
                    }
                });
            }

        } else {
            Chat.KIT.send(player, "&7You cannot change your kit at this time.");
        }

        return true;
    }
}
