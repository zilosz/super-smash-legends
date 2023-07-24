package com.github.zilosz.ssl.command;

import org.apache.commons.lang3.math.NumberUtils;

public class NumberValidator extends ArgumentValidator {

    public NumberValidator(String usageName) {
        super(usageName);
    }

    @Override
    public String getTypeHint() {
        return "Number";
    }

    @Override
    public boolean invalidate(String argument) {
        return !NumberUtils.isNumber(argument);
    }
}
