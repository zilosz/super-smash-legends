package com.github.zilosz.ssl.command;

public class StringValidator extends ArgumentValidator {

    public StringValidator(String usageName) {
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
