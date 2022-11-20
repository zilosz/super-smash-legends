package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.PermanentPotion;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.potion.PotionEffectType;

public class IceSkates extends PermanentPotion {

    public IceSkates(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public PotionEffectType getEffectType() {
        return PotionEffectType.SPEED;
    }
}
