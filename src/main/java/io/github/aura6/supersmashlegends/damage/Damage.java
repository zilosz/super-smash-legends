package io.github.aura6.supersmashlegends.damage;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.Vector;

public class Damage {
    @Getter @Setter private double damage;
    @Getter @Setter private boolean factorsArmor;
    @Getter @Setter private int immunityTicks;
    @Getter @Setter private Vector direction;
    @Getter @Setter private double kb;
    @Getter @Setter private double kbY;
    @Getter @Setter private boolean factorsHealth;
    @Getter @Setter private boolean factorsKb;

    private Damage(Builder builder) {
        this.damage = builder.damage;
        this.factorsArmor = builder.factorsArmor;
        this.immunityTicks = builder.immunityTicks;
        this.direction = builder.direction;
        this.kb = builder.kb;
        this.kbY = builder.kbY;
        this.factorsHealth = builder.factorsHealth;
        this.factorsKb = builder.factorsKb;
    }

    public static class Builder {
        private double damage;
        private boolean factorsArmor;
        private int immunityTicks;
        private Vector direction;
        private double kb;
        private double kbY;
        private boolean factorsHealth;
        private boolean factorsKb;

        public Builder setDamage(double damage) {
            this.damage = damage;
            return this;
        }

        public Builder setFactorsArmor(boolean factorsArmor) {
            this.factorsArmor = factorsArmor;
            return this;
        }

        public Builder setImmunityTicks(int immunityTicks) {
            this.immunityTicks = immunityTicks;
            return this;
        }

        public Builder setDirection(Vector direction) {
            this.direction = direction;
            return this;
        }

        public Builder setKb(double kb) {
            this.kb = kb;
            return this;
        }

        public Builder setKbY(double kbY) {
            this.kbY = kbY;
            return this;
        }

        public Builder setFactorsHealth(boolean factorsHealth) {
            this.factorsHealth = factorsHealth;
            return this;
        }

        public Builder setFactorsKb(boolean factorsKb) {
            this.factorsKb = factorsKb;
            return this;
        }

        public Damage build() {
            return new Damage(this);
        }

        public static Builder fromConfig(Section defaults, Vector direction) {
            return new Builder()
                    .setDamage(defaults.getDouble("Damage"))
                    .setKb(defaults.getDouble("Kb"))
                    .setKbY(defaults.getDouble("KbY"))
                    .setFactorsArmor(defaults.getOptionalBoolean("FactorsArmor").orElse(true))
                    .setImmunityTicks(defaults.getOptionalInt("ImmunityTicks").orElse(10))
                    .setFactorsHealth(defaults.getOptionalBoolean("FactorsHealth").orElse(true))
                    .setFactorsKb(defaults.getOptionalBoolean("FactorsKb").orElse(true))
                    .setDirection(direction);
        }
    }
}
