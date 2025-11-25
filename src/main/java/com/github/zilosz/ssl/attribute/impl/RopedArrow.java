package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.LeftClickAbility;
import com.github.zilosz.ssl.projectile.ArrowProjectile;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class RopedArrow extends LeftClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    new RopedProjectile(config, new AttackInfo(AttackType.ROPED_ARROW, this)).launch();
    player.getWorld().playSound(player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 2);
  }

  private static class RopedProjectile extends ArrowProjectile {

    public RopedProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onTick() {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_NORMAL)).show(entity.getLocation());
    }

    @Override
    public void onGeneralHit() {
      Vector direction = VectorUtils.fromTo(launcher, entity).normalize();
      Vector extra = new Vector(0, config.getDouble("ExtraY"), 0);
      launcher.setVelocity(direction.multiply(config.getDouble("PullStrength")).add(extra));
      launcher.playSound(launcher.getLocation(), Sound.STEP_WOOD, 1, 1);
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      ParticleBuilder particle =
          new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(200, 200, 200));
      new ParticleMaker(particle).boom(SSL.getInstance(), entity.getLocation(), 1.2, 0.4, 6);
    }
  }
}
