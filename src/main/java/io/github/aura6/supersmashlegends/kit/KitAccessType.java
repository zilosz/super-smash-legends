package io.github.aura6.supersmashlegends.kit;

import io.github.aura6.supersmashlegends.utils.message.MessageUtils;

public enum KitAccessType {
    ACCESS {
        @Override
        public String getLore(Kit kit) {
            return "&7You have access to this kit.";
        }
        @Override
        public String getHologram(Kit kit) {
            return "";
        }
    },

    ALREADY_SELECTED {
        @Override
        public String getLore(Kit kit) {
            return "&7This is your &dcurrent &7kit.";
        }
        @Override
        public String getHologram(Kit kit) {
            return MessageUtils.color("&dSelected");
        }
    };

    public abstract String getLore(Kit kit);

    public abstract String getHologram(Kit kit);
}
