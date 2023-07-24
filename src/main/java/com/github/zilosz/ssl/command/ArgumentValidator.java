package com.github.zilosz.ssl.command;

import lombok.Getter;

@Getter
public abstract class ArgumentValidator {
    private final String usageName;

    public ArgumentValidator(String usageName) {
        this.usageName = usageName;
    }

    public abstract String getTypeHint();

    public abstract boolean invalidate(String argument);

    @Override
    public String toString() {
        return String.format("'%s' of type '%s'", this.usageName, this.getTypeHint());
    }
}
