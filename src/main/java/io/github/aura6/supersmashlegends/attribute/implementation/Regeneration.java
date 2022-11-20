package io.github.aura6.supersmashlegends.attribute.implementation;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.RegenEvent;
import io.github.aura6.supersmashlegends.kit.Kit;

public class Regeneration extends Attribute {

    public Regeneration(SuperSmashLegends plugin, Kit kit) {
        super(plugin, kit);
        this.period = 20;
    }

    @Override
    public void run() {
        RegenEvent.attempt(player, kit.getRegen());
    }
}
