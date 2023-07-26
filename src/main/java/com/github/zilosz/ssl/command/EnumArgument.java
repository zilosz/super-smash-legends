package com.github.zilosz.ssl.command;

import org.apache.commons.lang3.EnumUtils;

public class EnumArgument<E extends Enum<E>> extends SimpleCommandArgument {
    private final Class<E> enumClass;

    public EnumArgument(String usageName, Class<E> enumClass) {
        super(usageName);
        this.enumClass = enumClass;
    }

    @Override
    public String getTypeHint() {
        return this.enumClass.getSimpleName();
    }

    @Override
    public boolean invalidate(String argument) {
        return !EnumUtils.isValidEnum(this.enumClass, argument.toUpperCase());
    }
}
