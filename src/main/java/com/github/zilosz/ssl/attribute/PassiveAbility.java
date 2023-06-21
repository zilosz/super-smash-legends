package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Setter;

public abstract class PassiveAbility extends Ability {
    @Setter private boolean removeOnActivation = true;

    public PassiveAbility(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void activate() {
        super.activate();

        if (this.removeOnActivation) {
            this.hotbarItem.destroy();
        }
    }
}
