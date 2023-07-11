package com.github.zilosz.ssl.damage;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.kit.KitManager;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;

@Getter
@Setter
public class Damage {
    private double damage;
    private boolean factorsArmor;

    public Damage(double damage, boolean factorsArmor) {
        this.damage = damage;
        this.factorsArmor = factorsArmor;
    }

    public double getFinalDamage(LivingEntity victim) {
        AtomicDouble damageValue = new AtomicDouble(this.damage);

        if (this.factorsArmor && victim instanceof Player) {
            KitManager kitManager = SSL.getInstance().getKitManager();
            Optional<Kit> optionalKit = Optional.ofNullable(kitManager.getSelectedKit((Player) victim));
            optionalKit.ifPresent(kit -> damageValue.set(damageValue.get() * kit.getArmor()));
        }

        return damageValue.get();
    }
}
