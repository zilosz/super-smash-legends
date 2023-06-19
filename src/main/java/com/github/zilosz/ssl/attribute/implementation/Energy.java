package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.kit.Kit;
import org.bukkit.Bukkit;

public class Energy extends Attribute {

    public Energy(SSL plugin, Kit kit) {
        super(plugin, kit);
    }

    @Override
    public void activate() {
        super.activate();
        this.player.setExp(1);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.player.setExp(0);
    }

    @Override
    public void run() {
        EnergyEvent energyEvent = new EnergyEvent(this.player, this.kit.getEnergy() / 20);
        Bukkit.getPluginManager().callEvent(energyEvent);
        this.player.setExp(Math.min(1, this.player.getExp() + energyEvent.getEnergy()));
    }
}
