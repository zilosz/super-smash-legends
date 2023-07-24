package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.utils.message.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CommandProcessor implements CommandExecutor {

    public abstract SenderRestriction getSenderRestriction();

    public abstract ArgumentValidator[] getRequiredValidators();

    public abstract ArgumentValidator[] getOptionalValidators();

    public abstract void processCommand(CommandSender sender, String[] arguments);

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (this.getSenderRestriction().invalidateSender(commandSender)) return true;

        ArgumentValidator[] required = this.getRequiredValidators();
        int i = 0;

        for (; i < required.length; i++) {
            ArgumentValidator validator = required[i];

            if (i == strings.length) {
                Chat.COMMAND.send(commandSender, String.format("&7Missing argument: %s", validator));
                return false;
            }

            if (validator.invalidate(strings[i])) {
                Chat.COMMAND.send(commandSender, String.format("&7Invalid required argument: %s", validator));
                return false;
            }
        }

        ArgumentValidator[] optional = this.getOptionalValidators();

        for (; i < optional.length && i < strings.length; i++) {
            ArgumentValidator validator = optional[i];

            if (validator.invalidate(strings[i])) {
                Chat.COMMAND.send(commandSender, String.format("&7Invalid optional argument: %s", validator));
                return false;
            }
        }

        this.processCommand(commandSender, strings);
        return true;
    }
}
