package com.github.zilosz.ssl.damage;

import com.github.zilosz.ssl.Resources;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.google.common.util.concurrent.AtomicDouble;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

@Getter
@Setter
public class KnockBack {
    private Vector direction;
    private double kb;
    private double kbY;
    private boolean factorsKit;
    private boolean factorsHealth;
    private boolean isLinear;
    private boolean factorsPreviousVelocity;

    public KnockBack(Vector direction, Section config) {
        this(
                direction,
                config.getDouble("Kb"),
                config.getDouble("KbY"),
                config.getOptionalBoolean("FactorsKit").orElse(true),
                config.getOptionalBoolean("FactorsHealth").orElse(true),
                config.getOptionalBoolean("IsLinear").orElse(false),
                config.getOptionalBoolean("FactorsPreviousVelocity").orElse(false)
        );
    }

    public KnockBack(Vector direction, double kb, double kbY, boolean factorsKit, boolean factorsHealth, boolean isLinear, boolean factorsPreviousVelocity) {
        this.direction = direction;
        this.kb = kb;
        this.kbY = kbY;
        this.factorsKit = factorsKit;
        this.factorsHealth = factorsHealth;
        this.isLinear = isLinear;
        this.factorsPreviousVelocity = factorsPreviousVelocity;
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
        AtomicDouble kbValue = new AtomicDouble(this.kb);

        if (this.factorsHealth) {
            Resources resources = SSL.getInstance().getResources();
            Section config = resources.getConfig().getSection("Damage");

            double health = victim.getHealth();
            double maxHealth = victim.getMaxHealth();

            kbValue.set(kbValue.get() * YamlReader.getDecreasingValue(config, "KbHealthMultiplier", health, maxHealth));
        }

        if (this.factorsKit && victim instanceof Player) {
            KitManager kitManager = SSL.getInstance().getKitManager();
            Optional<Kit> optionalKit = Optional.ofNullable(kitManager.getSelectedKit((Player) victim));
            optionalKit.ifPresent(kit -> kbValue.set(kbValue.get() * kit.getKb()));
        }

        return kbValue.get();
    }
}
