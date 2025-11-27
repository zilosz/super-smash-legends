package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PermanentPotion extends PassiveAbility {
  private PotionEffectType type;

  @Override
  public void activate() {
    super.activate();

    PotionEffect effect = YamlReader.potionEffect(config.getSection("Potion"));
    type = effect.getType();
    new PotionEffectEvent(player, effect).apply();
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (type != null) {
      player.removePotionEffect(type);
    }
  }

  @Override
  public String getUseType() {
    return "&oPassive";
  }
}
