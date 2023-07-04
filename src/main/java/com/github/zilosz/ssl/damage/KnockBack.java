package com.github.zilosz.ssl.damage;

import com.github.zilosz.ssl.Resources;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

@Getter
public class KnockBack {
    private Vector direction;
    private double kb;
    private double kbY;
    private boolean factorsKit;
    private boolean factorsHealth;
    private boolean isLinear;
    private boolean factorsPreviousVelocity;

    public KnockBack(Vector direction, double kb, double kbY, boolean factorsKit, boolean factorsHealth, boolean isLinear, boolean factorsPreviousVelocity) {
        this.direction = direction;
        this.kb = kb;
        this.kbY = kbY;
        this.factorsKit = factorsKit;
        this.factorsHealth = factorsHealth;
        this.isLinear = isLinear;
        this.factorsPreviousVelocity = factorsPreviousVelocity;
    }

    public KnockBack(Vector direction, Section config) {
        this.direction = direction;
        this.kb = config.getDouble("Kb");
        this.kbY = config.getDouble("KbY");
        this.factorsKit = config.getOptionalBoolean("FactorsKit").orElse(true);
        this.factorsHealth = config.getOptionalBoolean("FactorsHealth").orElse(true);
        this.isLinear = config.getOptionalBoolean("IsLinear").orElse(false);
        this.factorsPreviousVelocity = config.getOptionalBoolean("FactorsPreviousVelocity").orElse(false);
    }

    public KnockBack setDirection(Vector direction) {
        this.direction = direction;
        return this;
    }

    public KnockBack setKb(double kb) {
        this.kb = kb;
        return this;
    }

    public KnockBack setKbY(double kbY) {
        this.kbY = kbY;
        return this;
    }

    public KnockBack setFactorsKit(boolean factorsKit) {
        this.factorsKit = factorsKit;
        return this;
    }

    public KnockBack setFactorsHealth(boolean factorsHealth) {
        this.factorsHealth = factorsHealth;
        return this;
    }

    public KnockBack setLinear(boolean isLinear) {
        this.isLinear = isLinear;
        return this;
    }

    public KnockBack setFactorsPreviousVelocity(boolean factorsPreviousVelocity) {
        this.factorsPreviousVelocity = factorsPreviousVelocity;
        return this;
    }

    public Optional<Vector> getFinalKbVector(LivingEntity victim) {
        Vector dir = this.direction;

        if (dir == null) {
            return Optional.empty();
        }

        double finalKb = this.getFinalKb(victim);
        Vector velocity = dir.clone().setY(0).normalize().multiply(new Vector(finalKb, 1, finalKb));
        velocity.setY(this.isLinear ? dir.getY() : this.kbY);

        if (this.factorsPreviousVelocity) {
            velocity = victim.getVelocity().add(velocity);
        }

        return Optional.of(velocity);
    }

    public double getFinalKb(LivingEntity victim) {
        double kbVal = this.kb;

        if (this.factorsHealth) {
            Resources resources = SSL.getInstance().getResources();
            Section config = resources.getConfig().getSection("Damage");

            double health = victim.getHealth();
            double maxHealth = victim.getMaxHealth();

            kbVal *= YamlReader.getDecreasingValue(config, "KbHealthMultiplier", health, maxHealth);
        }

        if (this.factorsKit && victim instanceof Player) {
            KitManager kitManager = SSL.getInstance().getKitManager();
            kbVal *= kitManager.getSelectedKit((Player) victim).getKb();
        }

        return kbVal;
    }
}
