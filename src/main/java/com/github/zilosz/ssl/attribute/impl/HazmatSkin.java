package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.PermanentPotion;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class HazmatSkin extends PermanentPotion {
  private final Set<PotionEffectType> resistedEffects = new HashSet<>();

  @Override
  public void activate() {
    super.activate();

    for (String effect : config.getStringList("ResistedPotionEffects")) {
      resistedEffects.add(PotionEffectType.getByName(effect));
    }
  }

  @EventHandler
  public void onPotionEffect(PotionEffectEvent event) {
    if (event.getEntity() == player && resistedEffects.contains(event.getType())) {
      event.setCancelled(true);
    }
  }
}
