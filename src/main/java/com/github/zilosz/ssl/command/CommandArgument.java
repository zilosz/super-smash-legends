package com.github.zilosz.ssl.command;

public interface CommandArgument {

    String getUsageName();

    String getTypeHint();

    boolean invalidate(String argument);
}
