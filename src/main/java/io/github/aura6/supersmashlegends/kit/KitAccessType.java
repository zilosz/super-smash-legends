package io.github.aura6.supersmashlegends.kit;

public enum KitAccessType {
    ACCESS {
        @Override
        public String getLore(Kit kit) {
            return "&7You have access to this kit.";
        }
        @Override
        public String getSuffix(Kit kit) {
            return "";
        }
    },

    ALREADY_SELECTED {
        @Override
        public String getLore(Kit kit) {
            return "&7This is your &dcurrent &7kit.";
        }
        @Override
        public String getSuffix(Kit kit) {
            return "&7- &d&lSelected";
        }
    },

    BUY {
        @Override
        public String getLore(Kit kit) {
            return String.format("&7Click to buy for &f%d &7jewels.", kit.getPrice());
        }
        @Override
        public String getSuffix(Kit kit) {
            return String.format("&7- &5&l%d Jewels", kit.getPrice());
        }
    };

    public abstract String getLore(Kit kit);

    public abstract String getSuffix(Kit kit);
}
