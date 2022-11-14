package io.github.aura6.supersmashlegends.attribute.implementation;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.EnergyEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import org.bukkit.Bukkit;

public class Energy extends Attribute {

    public Energy(SuperSmashLegends plugin, Kit kit) {
        super(plugin, kit);
    }

    @Override
    public void activate() {
        super.activate();
        player.setExp(1);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        player.setExp(0);
    }

    @Override
    public void run() {
        EnergyEvent energyEvent = new EnergyEvent(player, kit.getEnergy() / 20);
        Bukkit.getPluginManager().callEvent(energyEvent);
        player.setExp(Math.min(1, player.getExp() + energyEvent.getEnergy()));
    }
}
