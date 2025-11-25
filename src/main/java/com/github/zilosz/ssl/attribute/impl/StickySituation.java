package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.event.attribute.RegenEvent;
import com.github.zilosz.ssl.util.effects.Effects;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffectType;

public class StickySituation extends PassiveAbility {
  private boolean active;

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @Override
  public String getUseType() {
    return "Enter Low Health";
  }

  public void reset() {
    active = false;
    player.removePotionEffect(PotionEffectType.SPEED);
  }

  @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true)
  public void onReceivingDamage(DamageEvent event) {
    if (event.getVictim() != player) return;
    if (active) return;
    if (event.willDie() || event.getNewHealth() > config.getDouble("HealthThreshold")) return;

    active = true;

    int speed = config.getInt("Speed");
    new PotionEffectEvent(player, PotionEffectType.SPEED, Integer.MAX_VALUE, speed);

    player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 1);

    FireworkEffect.Builder settings = FireworkEffect
        .builder()
        .withColor(Color.fromRGB(0, 255, 0))
        .with(FireworkEffect.Type.BALL)
        .trail(true);

    Effects.launchFirework(player.getEyeLocation(), settings, 1);
  }

  @EventHandler
  public void onRegen(RegenEvent event) {
    if (event.getPlayer() != player) return;
    if (!active) return;
    if (player.getHealth() + event.getRegen() <= config.getDouble("HealthThreshold")) {
      return;
    }

    reset();

    player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_DEATH, 1, 1);

    FireworkEffect.Builder settings = FireworkEffect
        .builder()
        .withColor(Color.fromRGB(0, 120, 0))
        .with(FireworkEffect.Type.BURST)
        .trail(true);

    Effects.launchFirework(player.getEyeLocation(), settings, 1);
  }

  @EventHandler
  public void onEnergy(EnergyEvent event) {
    if (event.getPlayer() == player && active) {
      event.setEnergy(event.getEnergy() * config.getFloat("EnergyMultiplier"));
    }
  }
}
