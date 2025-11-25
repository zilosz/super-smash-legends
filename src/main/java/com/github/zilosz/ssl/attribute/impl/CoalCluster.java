package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class CoalCluster extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    AttackInfo attackInfo = new AttackInfo(AttackType.COAL_CLUSTER, this);
    new ClusterProjectile(config.getSection("Cluster"), attackInfo).launch();
    player.getWorld().playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 2);
  }

  private static class ClusterProjectile extends BlockProjectile {

    public ClusterProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      breakIntoFragments();
    }

    @Override
    public void onTargetHit(LivingEntity victim) {
      breakIntoFragments();

      if (MathUtils.rollChance(config.getDouble("BurnChance"))) {
        victim.setFireTicks(config.getInt("BurnDuration"));
      }
    }

    @Override
    public void onTick() {
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
      new ParticleMaker(particle).show(entity.getLocation());
    }

    private void breakIntoFragments() {
      entity.getWorld().playSound(entity.getLocation(), Sound.EXPLODE, 2, 1);

      for (int i = 0; i < 3; i++) {
        new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(entity.getLocation());
      }

      float yawStep = 360f / (config.getInt("FragmentCount") - 1);
      Location launchLoc = entity.getLocation().add(0, 0.5, 0);

      for (int i = 0; i < config.getInt("FragmentCount"); i++) {
        float minPitch = config.getFloat("MinPitch");
        float maxPitch = config.getFloat("MaxPitch");
        launchLoc.setPitch((float) MathUtils.randRange(minPitch, maxPitch));
        launchLoc.setYaw(i * yawStep);

        Section settings = config.getSection("Fragment");
        FragmentProjectile projectile = new FragmentProjectile(settings, attackInfo);
        projectile.setOverrideLocation(launchLoc);
        projectile.launch();
      }
    }
  }

  private static class FragmentProjectile extends ItemProjectile {

    public FragmentProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_NORMAL)).show(entity.getLocation());
    }

    @Override
    public void onTargetHit(LivingEntity victim) {
      if (MathUtils.rollChance(config.getDouble("BurnChance"))) {
        victim.setFireTicks(config.getInt("BurnDuration"));
      }
    }

    @Override
    public void onTick() {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.FLAME)).show(entity.getLocation());
    }
  }
}
