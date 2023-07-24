package com.github.zilosz.ssl.command;

import org.apache.commons.lang3.EnumUtils;

public class EnumValidator<E extends Enum<E>> extends ArgumentValidator {
    private final Class<E> enumClass;

    public EnumValidator(String usageName, Class<E> enumClass) {
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
