package com.github.zilosz.ssl.command;

import org.bukkit.Bukkit;

public class PlayerArgument extends SimpleCommandArgument {

    public PlayerArgument(String usageName) {
        super(usageName);
    }

    @Override
    public String getTypeHint() {
        return "Player";
    }

    @Override
    public boolean invalidate(String argument) {
        return Bukkit.getPlayer(argument) == null;
    }
}
