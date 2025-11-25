package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Jetpack extends PassiveAbility {

  @Override
  public String getUseType() {
    return "Sneak";
  }

  @Override
  public void run() {
    if (!player.isSneaking() || player.getExp() < config.getFloat("EnergyPerTick")) return;
    player.setExp(player.getExp() - config.getFloat("EnergyPerTick"));

    player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 2, 2);

    Vector direction = player.getEyeLocation().getDirection();
    double velocity = config.getDouble("Velocity");
    Vector multiplier = new Vector(velocity, 1, velocity);
    player.setVelocity(direction.multiply(multiplier).setY(config.getDouble("VelocityY")));

    Location location = player.getLocation();

    double particleX = location.getX();
    double particleY = location.getY() - config.getDouble("StreamFeetDistance");
    double particleZ = location.getZ();

    float spread = config.getFloat("MaxStreamSpread");

    while (spread > 0) {

      for (int i = 0; i < config.getDouble("ParticlesPerSpread") * spread; i++) {
        Location loc = new Location(player.getWorld(), particleX, particleY, particleZ);
        ParticleBuilder flame = new ParticleBuilder(ParticleEffect.FLAME);
        new ParticleMaker(flame).setSpread(spread, 0, spread).show(loc);
      }

      spread -= config.getDouble("StreamSpreadStep");
      particleY -= config.getDouble("StreamStep");
    }
  }

  @EventHandler
  public void onEnergy(EnergyEvent event) {
    if (event.getPlayer() != player) return;

    if (player.isSneaking() || !EntityUtils.isPlayerGrounded(player)) {
      event.setEnergy(0);
    }
  }
}
