package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.kit.KitManager;
import com.github.zilosz.ssl.util.math.MathUtils;
import com.google.common.util.concurrent.AtomicDouble;
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
    private double minHealthBoost;
    private double maxHealthBoost;

    public KnockBack(Vector direction, double kb, double kbY, boolean factorsKit, boolean factorsHealth, boolean isLinear, boolean factorsPreviousVelocity, double minHealthBoost, double maxHealthBoost) {
        this.direction = direction;
        this.kb = kb;
        this.kbY = kbY;
        this.factorsKit = factorsKit;
        this.factorsHealth = factorsHealth;
        this.isLinear = isLinear;
        this.factorsPreviousVelocity = factorsPreviousVelocity;
        this.minHealthBoost = minHealthBoost;
        this.maxHealthBoost = maxHealthBoost;
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
            double multiplier = MathUtils.decreasingValue(
                    this.minHealthBoost, this.maxHealthBoost, victim.getMaxHealth(), victim.getHealth());
            kbValue.set(kbValue.get() * multiplier);
        }

        if (this.factorsKit && victim instanceof Player) {
            KitManager kitManager = SSL.getInstance().getKitManager();
            Optional<Kit> optionalKit = Optional.ofNullable(kitManager.getSelectedKit((Player) victim));
            optionalKit.ifPresent(kit -> kbValue.set(kbValue.get() * kit.getKb()));
        }

        return kbValue.get();
    }
}
