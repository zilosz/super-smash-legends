package com.github.zilosz.ssl.command.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.command.ArgumentValidator;
import com.github.zilosz.ssl.command.CommandProcessor;
import com.github.zilosz.ssl.command.EnumValidator;
import com.github.zilosz.ssl.command.SenderRestriction;
import com.github.zilosz.ssl.game.GameManager;
import com.github.zilosz.ssl.kit.KitSelector;
import com.github.zilosz.ssl.kit.KitType;
import com.github.zilosz.ssl.utils.message.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand extends CommandProcessor {

    @Override
    public SenderRestriction getSenderRestriction() {
        return SenderRestriction.PLAYER_ONLY;
    }

    @Override
    public ArgumentValidator[] getRequiredValidators() {
        return new ArgumentValidator[0];
    }

    @Override
    public ArgumentValidator[] getOptionalValidators() {
        return new ArgumentValidator[]{new EnumValidator<>("kit", KitType.class)};
    }

    @Override
    public void processCommand(CommandSender sender, String[] arguments) {
        Player player = (Player) sender;
        GameManager gameManager = SSL.getInstance().getGameManager();

        if (gameManager.isSpectator(player)) {
            Chat.GAME.send(player, "&7You can't use this command in spectator mode.");
            return;
        }

        if (!gameManager.getState().allowsKitSelection()) {
            Chat.KIT.send(player, "&7You cannot change your kit at this time.");
            return;
        }

        if (arguments.length == 0) {
            new KitSelector().build().open((Player) sender);

        } else {
            SSL.getInstance().getKitManager().setKit(player, KitType.valueOf(arguments[0].toUpperCase()));
        }
    }
}
