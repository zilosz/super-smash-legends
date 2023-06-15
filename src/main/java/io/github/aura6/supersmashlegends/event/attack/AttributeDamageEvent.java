package io.github.aura6.supersmashlegends.event.damage;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.damage.DamageSettings;
import io.github.aura6.supersmashlegends.kit.KitManager;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageEvent extends SingleAttackEvent {
    @Getter private final DamageSettings damageSettings;
    @Getter private final boolean isVoid;

    public DamageEvent(LivingEntity victim, Attribute attribute, DamageSettings damageSettings, boolean isVoid) {
        super(victim, attribute);
        this.damageSettings = damageSettings;
        this.isVoid = isVoid;
    }

    public double getFinalDamage() {
        if (this.isVoid) return this.victim.getHealth();

        double damageValue = this.damageSettings.getDamage();

        if (this.damageSettings.isFactorsArmor() && this.victim instanceof Player) {
            KitManager kitManager = SuperSmashLegends.getInstance().getKitManager();
            damageValue *= kitManager.getSelectedKit((Player) this.victim).getArmor();
        }

        return damageValue;
    }
}
