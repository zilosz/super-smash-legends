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
    },

    BUY {
        @Override
        public String getLore(Kit kit) {
            return String.format("&7Click to buy for &f%d &7jewels.", kit.getPrice());
        }
        @Override
        public String getHologram(Kit kit) {
            return MessageUtils.color(String.format("&e%d &5Jewels", kit.getPrice()));
        }
    };

    public abstract String getLore(Kit kit);

    public abstract String getHologram(Kit kit);
}
