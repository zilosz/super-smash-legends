package com.github.zilosz.ssl.command;

import com.github.zilosz.ssl.utils.message.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum SenderRestriction {
    NONE {
        @Override
        public boolean invalidateSender(CommandSender sender) {
            return false;
        }
    },
    PLAYER_ONLY {
        @Override
        public boolean invalidateSender(CommandSender sender) {
            if (sender instanceof Player) return false;
            Chat.COMMAND.send(sender, "&7This command is player-only!");
            return true;
        }
    },
    CONSOLE_ONLY {
        @Override
        public boolean invalidateSender(CommandSender sender) {
            if (sender instanceof Player) {
                Chat.COMMAND.send(sender, "&7This command is console-only!");
                return true;
            }
            return false;
        }
    };

    public abstract boolean invalidateSender(CommandSender sender);
}
