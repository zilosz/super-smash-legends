package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class PermanentPotion extends PassiveAbility {

    public PermanentPotion(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    public abstract PotionEffectType getEffectType();

    @Override
    public String getUseType() {
        return "Potion Effect";
    }

    @Override
    public void activate() {
        super.activate();
        player.addPotionEffect(new PotionEffect(getEffectType(), Integer.MAX_VALUE, config.getInt("Amplifier")));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        player.removePotionEffect(getEffectType());
    }
}
