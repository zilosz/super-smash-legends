package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.util.RunnableUtils;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.block.BlockUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.FloatingEntity;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class GhoulishWrath extends ChargedRightClickAbility {
  private FloatingEntity<FallingBlock> floatingBlock;
  private float pitch;

  @Override
  public void onFailedCharge() {
    player.getWorld().playSound(floatingBlock.getEntity().getLocation(), Sound.DIG_SAND, 2, 0.8f);
    reset();
  }

  @Override
  public void onSuccessfulCharge() {
    int count = config.getInt("BlockCount");
    int interval = config.getInt("LaunchInterval");
    int ticksCharged = ticksCharging;

    AttackInfo attackInfo = new AttackInfo(AttackType.GHOULISH_WRATH, this);
    double speed = YamlReader.incVal(config, "Velocity", ticksCharged, getMaxChargeTicks());

    RunnableUtils.runIntervaledTask(SSL.getInstance(), count, interval, () -> {
      player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 0.5f, 2);

      SoulProjectile projectile = new SoulProjectile(config.getSection("Projectile"), attackInfo);
      projectile.setSpeed(speed);
      projectile.launch();
    });

    reset();
  }

  @Override
  public void onChargeTick() {
    floatingBlock.teleport(getFloatingLocation());
    player.playSound(floatingBlock.getEntity().getLocation(), Sound.FIREWORK_LAUNCH, 1, pitch);
    pitch += 1.5f / getMaxChargeTicks();
  }

  @Override
  public void onInitialClick(PlayerInteractEvent event) {
    Material material = Material.valueOf(config.getString("Projectile.Block.Material"));
    FallingBlock block = BlockUtils.spawnFallingBlock(getFloatingLocation(), material);
    floatingBlock = FloatingEntity.fromEntity(block);

    pitch = 0.5f;
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  private Location getFloatingLocation() {
    Location eyeLoc = player.getEyeLocation();
    return eyeLoc.add(eyeLoc.getDirection().multiply(config.getDouble("EyeDistance")));
  }

  private void reset() {
    if (floatingBlock != null) {
      floatingBlock.destroy();
    }
  }

  private static class SoulProjectile extends BlockProjectile {

    public SoulProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      playEffect();

      for (int i = 0; i < 7; i++) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SPELL_WITCH);
        new ParticleMaker(particle).setSpread(0.75f, 0.75f, 0.75f).show(entity.getLocation());
      }
    }

    @Override
    public void onPreTargetHit(LivingEntity target) {
      String attackPath = target.hasMetadata("pumpkin") ? "PumpkinAttack" : "NormalAttack";
      String name = ((Ability) attackInfo.getAttribute()).getDisplayName();
      attack = YamlReader.attack(config.getSection(attackPath), null, name);
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      playEffect();

      if (target.hasMetadata("pumpkin")) {
        new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(entity.getLocation());
        entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_HURT, 1, 1);
      }
    }

    @Override
    public void onTick() {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK)).show(entity.getLocation());
    }

    private void playEffect() {
      entity.getWorld().playSound(entity.getLocation(), Sound.DIG_GRAVEL, 3, 0.5f);
    }
  }
}
