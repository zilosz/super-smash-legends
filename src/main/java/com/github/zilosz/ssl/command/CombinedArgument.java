package com.github.zilosz.ssl.command;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CombinedArgument implements CommandArgument {
    private final CommandArgument[] arguments;

    public CombinedArgument(CommandArgument... arguments) {
        this.arguments = arguments;
    }

    private String split(Function<CommandArgument, String> func) {
        return StringUtils.join(Arrays.stream(this.arguments).map(func).collect(Collectors.toList()), '/');
    }

    @Override
    public String getUsageName() {
        return this.split(CommandArgument::getUsageName);
    }

    @Override
    public String getTypeHint() {
        return this.split(CommandArgument::getTypeHint);
    }

    @Override
    public boolean invalidate(String argument) {
        return Arrays.stream(this.arguments).allMatch(arg -> arg.invalidate(argument));
    }
}
