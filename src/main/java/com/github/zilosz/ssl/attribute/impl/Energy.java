package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import org.bukkit.Bukkit;

public class Energy extends Attribute {

  @Override
  public void activate() {
    if (givesEnergy()) {
      super.activate();
      player.setExp(1);
    }
  }

  private boolean givesEnergy() {
    return kit.getEnergyValue() > 0;
  }

  @Override
  public void run() {
    if (player.getExp() < 1) {
      EnergyEvent energyEvent = new EnergyEvent(player, kit.getEnergyValue() / 20);
      Bukkit.getPluginManager().callEvent(energyEvent);
      player.setExp(Math.min(1, player.getExp() + energyEvent.getEnergy()));
    }
  }

  @Override
  public void deactivate() {
    if (givesEnergy()) {
      super.deactivate();
      player.setExp(0);
    }
  }
}
