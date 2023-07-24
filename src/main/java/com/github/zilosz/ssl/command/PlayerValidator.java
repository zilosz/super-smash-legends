package com.github.zilosz.ssl.command;

import org.bukkit.Bukkit;

public class PlayerValidator extends ArgumentValidator {

    public PlayerValidator(String usageName) {
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
