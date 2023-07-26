package com.github.zilosz.ssl.command;

public class StringArgument extends SimpleCommandArgument {

    public StringArgument(String usageName) {
        super(usageName);
    }

    @Override
    public String getTypeHint() {
        return "String";
    }

    @Override
    public boolean invalidate(String argument) {
        return false;
    }
}
