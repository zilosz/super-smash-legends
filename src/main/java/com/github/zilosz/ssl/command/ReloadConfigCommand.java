package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.Resources;
import com.github.zilosz.ssl.utils.message.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadConfigCommand implements CommandExecutor {
    private final Resources resources;

    public ReloadConfigCommand(Resources resources) {
        this.resources = resources;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        this.resources.reload();
        Chat.COMMAND.broadcast("&7Configuration has been reloaded.");
        return true;
    }
}
