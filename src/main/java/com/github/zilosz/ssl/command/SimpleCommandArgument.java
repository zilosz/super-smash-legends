package com.github.zilosz.ssl.command;

public abstract class SimpleCommandArgument implements CommandArgument {
    private final String usageName;

    public SimpleCommandArgument(String usageName) {
        this.usageName = usageName;
    }

    @Override
    public String getUsageName() {
        return this.usageName;
    }
}
