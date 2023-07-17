package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import org.bukkit.Bukkit;

public class Energy extends Attribute {

    private boolean givesEnergy() {
        return this.kit.getEnergyValue() > 0;
    }

    @Override
    public void activate() {
        super.activate();

        if (this.givesEnergy()) {
            this.player.setExp(1);
        }
    }

    @Override
    public void run() {
        if (this.givesEnergy() && this.player.getExp() < 1) {
            EnergyEvent energyEvent = new EnergyEvent(this.player, this.kit.getEnergyValue() / 20);
            Bukkit.getPluginManager().callEvent(energyEvent);
            this.player.setExp(Math.min(1, this.player.getExp() + energyEvent.getEnergy()));
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.givesEnergy()) {
            this.player.setExp(0);
        }
    }
}
