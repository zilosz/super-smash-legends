package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;

public abstract class PassiveAbility extends Ability {

    public PassiveAbility(SuperSmashLegends plugin, Section config, Kit kit, int slot) {
        super(plugin, config, kit, slot);
    }

    public String getUseDescription() {
        return config.getString("UseDescription");
    }

    @Override
    public void activate() {
        super.activate();
        hotbarItem.destroy();
    }
}
