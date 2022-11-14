package io.github.aura6.supersmashlegends.attribute;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.kit.Kit;
import lombok.Setter;

public abstract class PassiveAbility extends Ability {
    @Setter private boolean removeOnActivation = true;

    public PassiveAbility(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void activate() {
        super.activate();

        if (removeOnActivation) {
            hotbarItem.destroy();
        }
    }
}
