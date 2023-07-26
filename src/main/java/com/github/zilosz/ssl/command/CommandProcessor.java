package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.utils.message.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CommandProcessor implements CommandExecutor {

    public abstract SenderRestriction getSenderRestriction();

    public abstract CommandArgument[] getRequiredArgs();

    public abstract CommandArgument[] getOptionalArgs();

    public abstract void processCommand(CommandSender sender, String[] arguments);

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (this.getSenderRestriction().invalidateSender(commandSender)) return true;

        CommandArgument[] required = this.getRequiredArgs();
        int i = 0;

        for (; i < required.length; i++) {
            CommandArgument arg = required[i];

            if (i == strings.length) {
                Chat.COMMAND.send(commandSender, String.format("&7Missing argument: '%s'", arg.getUsageName()));
                return false;
            }

            if (arg.invalidate(strings[i])) {
                String message = "&7Invalid required argument: '%s' is not a valid &o%s.";
                Chat.COMMAND.send(commandSender, String.format(message, arg.getUsageName(), arg.getTypeHint()));
                return false;
            }
        }

        CommandArgument[] optional = this.getOptionalArgs();

        for (; i < optional.length && i < strings.length; i++) {
            CommandArgument arg = optional[i];

            if (arg.invalidate(strings[i])) {
                String message = "&7Invalid optional argument: '%s' is not a valid &o%s.";
                Chat.COMMAND.send(commandSender, String.format(message, arg.getUsageName(), arg.getTypeHint()));
                return false;
            }
        }

        this.processCommand(commandSender, strings);
        return true;
    }
}
