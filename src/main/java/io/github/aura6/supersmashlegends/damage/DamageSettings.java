package io.github.aura6.supersmashlegends.damage;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.KitManager;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@Getter
public class DamageSettings {
    private double damage;
    private boolean factorsArmor;

    public DamageSettings(double damage, boolean factorsArmor) {
        this.damage = damage;
        this.factorsArmor = factorsArmor;
    }

    public DamageSettings(Section config) {
        this(config.getDouble("Damage"), config.getOptionalBoolean("FactorsArmor").orElse(true));
    }

    public DamageSettings setDamage(double damage) {
        this.damage = damage;
        return this;
    }

    public DamageSettings setFactorsArmor(boolean factorsArmor) {
        this.factorsArmor = factorsArmor;
        return this;
    }

    public double getFinalDamage(LivingEntity victim) {
        double damageValue = this.damage;

        if (this.factorsArmor && victim instanceof Player) {
            KitManager kitManager = SuperSmashLegends.getInstance().getKitManager();
            damageValue *= kitManager.getSelectedKit((Player) victim).getArmor();
        }

        return damageValue;
    }
}
