package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.block.BlockRay;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Teleport extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    Location loc = player.getEyeLocation();

    BlockRay blockRay = new BlockRay(loc, loc.getDirection());
    blockRay.cast(config.getInt("Range"));
    player.teleport(blockRay.getEmptyLoc());

    player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 1, 2);
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
    new ParticleMaker(particle).solidSphere(loc, 1.1, 10, 0.3);
  }
}
