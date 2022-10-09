package io.github.zilosz.newsmashplugin.utils.message;

public class Replacer {
    private final String placeholder;
    private final String replacement;

    public Replacer(String placeholder, String replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    public String replace(String message) {
        return message.replace("{" + placeholder + "}", replacement);
    }
}
