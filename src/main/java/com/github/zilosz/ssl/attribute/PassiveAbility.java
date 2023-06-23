package com.github.zilosz.ssl.attribute;

import lombok.Setter;

public abstract class PassiveAbility extends Ability {
    @Setter private boolean removeOnActivation = true;

    @Override
    public void activate() {
        super.activate();

        if (this.removeOnActivation) {
            this.hotbarItem.destroy();
        }
    }
}
