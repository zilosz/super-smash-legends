package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.kit.KitSelector;
import com.github.zilosz.ssl.kit.KitType;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.message.Chat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;

        Player player = (Player) commandSender;
        GameManager gameManager = SSL.getInstance().getGameManager();

        if (gameManager.isSpectator(player)) {
            Chat.GAME.send(player, "&7You can't use this command in spectator mode.");
            return true;
        }

        if (!gameManager.getState().allowsKitSelection()) {
            Chat.KIT.send(player, "&7You cannot change your kit at this time.");
            return true;
        }

        if (strings.length == 0) {
            new KitSelector().build().open(player);
            return true;
        }

        if (strings.length > 1) {
            return false;
        }

        KitManager kitManager = SSL.getInstance().getKitManager();
        String name = StringUtils.capitalize(strings[0].toLowerCase());

        if (name.equalsIgnoreCase("random")) {
            Chat.KIT.send(player, "&7Selecting a random kit...");
            kitManager.setKit(player, CollectionUtils.selectRandom(KitType.values()));

        } else {

            try {
                kitManager.setKit(player, KitType.valueOf(name.toUpperCase()));

            } catch (IllegalArgumentException e) {
                Chat.KIT.send(player, String.format("&7\"%s\" &7is not a valid kit.", name));
            }
        }

        return true;
    }
}
