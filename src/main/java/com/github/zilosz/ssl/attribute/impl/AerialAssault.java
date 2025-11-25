package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class AerialAssault extends ChargedRightClickAbility {
  private Vector velocity;

  @Override
  public void onChargeTick() {
    if (EntityUtils.isPlayerGrounded(player)) return;

    player.setVelocity(velocity);
    player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 2);

    player.setVelocity(velocity);
    Vector forward = velocity.clone().normalize().multiply(2);
    Location particleCenter = EntityUtils.center(player).setDirection(velocity).add(forward);

    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK);
    new ParticleMaker(particle).ring(particleCenter, 1.5, 20);

    EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

    new EntityFinder(selector).findAll(player).forEach(target -> {
      Attack attack = YamlReader.attack(config, velocity, getDisplayName());
      AttackInfo attackInfo = new AttackInfo(AttackType.AERIAL_ASSAULT, this);

      if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
        player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 1, 1);
      }
    });
  }

  @Override
  public void onInitialClick(PlayerInteractEvent event) {
    double speed = config.getDouble("Speed");
    double y = config.getDouble("VelocityY");
    velocity = player.getEyeLocation().getDirection().multiply(speed).setY(y);
  }
}
