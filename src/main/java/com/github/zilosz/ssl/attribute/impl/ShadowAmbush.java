package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class ShadowAmbush extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
    new ParticleMaker(particle).solidSphere(EntityUtils.center(player), 1.5, 15, 0.5);

    EntityFinder finder = new EntityFinder(new DistanceSelector(config.getDouble("Range")));

    finder.findClosest(player).ifPresentOrElse(target -> {
      Location targetLoc = target.getLocation();
      Vector targetDir = targetLoc.getDirection().setY(0).normalize();
      Location spotBehind = targetLoc.subtract(targetDir).setDirection(targetDir);

      Block one = spotBehind.getBlock();
      Block two = spotBehind.clone().add(0, 1, 0).getBlock();

      player.teleport(one.getType().isSolid() || two.getType().isSolid() ? targetLoc : spotBehind);

      int duration = config.getInt("BlindnessDuration");
      new PotionEffectEvent(target, PotionEffectType.BLINDNESS, duration, 1).apply();

      player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 1, 0.5f);

      ParticleBuilder particle2 = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
      new ParticleMaker(particle2).solidSphere(EntityUtils.center(player), 2.5, 15, 0.5);

    }, () -> player.playSound(player.getLocation(), Sound.WITHER_HURT, 1, 2f));
  }
}
