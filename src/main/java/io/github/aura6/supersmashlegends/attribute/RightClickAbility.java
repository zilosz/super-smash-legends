package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;

public abstract class RightClickAbility extends ClickableAbility {

    public RightClickAbility(SuperSmashLegends plugin, Section config, Kit kit, int slot) {
        super(plugin, config, kit, slot);
    }

    @Override
    public String getUseType() {
        return "Right Click";
    }
}
