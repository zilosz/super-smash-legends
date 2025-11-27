package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickBlockAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.block.BlockUtils;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.FloatingEntity;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.MathUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GaiaToss extends ChargedRightClickBlockAbility {
  private final List<FloatingEntity<FallingBlock>> blocks = new ArrayList<>();
  private final Collection<BukkitTask> positionUpdaters = new ArrayList<>();
  private int currSize;
  private int increments = -1;
  private BukkitTask soundTask;
  private BukkitTask sizeTask;

  @Override
  public void onFailedCharge() {
    playSound();
    reset(true);
  }

  @Override
  public void onSuccessfulCharge() {
    player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 4, 0.5f);

    int minSize = config.getInt("MinSize");
    int val = currSize - minSize;
    int limit = getMaxSize() - minSize;

    double speed = YamlReader.incVal(config, "Speed", val, limit);
    double damage = YamlReader.incVal(config, "Damage", val, limit);
    double kb = YamlReader.incVal(config, "Kb", val, limit);

    launch(true, damage, kb, speed, blocks.get(0).getEntity());

    for (int i = 1; i < blocks.size(); i++) {
      launch(false, damage, kb, speed, blocks.get(i).getEntity());
    }

    reset(true);
  }

  @Override
  public void onInitialClick(PlayerInteractEvent event) {

    soundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      double pitch = MathUtils.incVal(0.5, 2, getMaxChargeTicks(), ticksCharging);
      player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, (float) pitch);
    }, 0, 3);

    currSize = config.getInt("MinSize") - config.getInt("IncrementSize");

    Vector direction = player.getEyeLocation().getDirection().multiply(getMaxSize() / 2.0);
    Location centerInGround = event.getClickedBlock().getLocation().add(direction);

    sizeTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      if (++increments > config.getInt("IncrementCount")) return;

      currSize += config.getInt("IncrementSize");

      playSound();
      destroyBlocks();

      List<Location> locations = MathUtils.locationCube(centerInGround, currSize);
      double heightAboveHead = currSize / 2.0 + config.getDouble("HeightAboveHead");
      Location centerAboveHead = EntityUtils.top(player).add(new Vector(0, heightAboveHead, 0));
      Vector relative = VectorUtils.fromTo(locations.get(0), centerAboveHead);

      for (Location loc : locations) {
        Block block = loc.getBlock();
        Material type = block.getType();

        if (!type.isSolid()) {
          type = Material.IRON_BLOCK;
        }

        Location carryLoc = loc.add(relative);

        FallingBlock entity = BlockUtils.spawnFallingBlock(carryLoc, type, block.getData());
        FloatingEntity<FallingBlock> floatingBlock = FloatingEntity.fromEntity(entity);
        blocks.add(floatingBlock);

        Vector relativeToLoc = VectorUtils.fromTo(player.getLocation(), carryLoc);

        positionUpdaters.add(Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
          floatingBlock.teleport(player.getLocation().add(relativeToLoc));
        }, 0, 0));
      }
    }, 0, (long) (getMaxChargeTicks() / (config.getDouble("IncrementCount") + 1)));
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (increments != -1) {
      reset(false);
    }
  }

  private int getMaxSize() {
    int minSize = config.getInt("MinSize");
    int incrementCount = config.getInt("IncrementCount");
    int incrementSize = config.getInt("IncrementSize");
    return minSize + incrementCount * incrementSize;
  }

  private void launch(
      boolean particles, double damage, double kb, double speed, FallingBlock block
  ) {
    AttackInfo attackInfo = new AttackInfo(AttackType.GAIA_TOSS, this);
    GaiaTossProjectile projectile = new GaiaTossProjectile(config, attackInfo, particles);

    projectile.setMaterial(block.getMaterial());
    projectile.setData(block.getBlockData());

    Vector dir = player.getEyeLocation().getDirection();
    projectile.setOverrideLocation(block.getLocation().setDirection(dir));

    projectile.setSpeed(speed);

    projectile.getAttack().getDamage().setDamage(damage);
    projectile.getAttack().getKb().setKb(kb);

    projectile.launch();
  }

  private void playSound() {
    blocks.forEach(block -> {
      Entity entity = block.getEntity();
      entity.getWorld().playSound(entity.getLocation(), Sound.DIG_GRASS, 1, 1);
    });
  }

  private void reset(boolean cooldown) {
    increments = -1;

    soundTask.cancel();
    sizeTask.cancel();

    destroyBlocks();

    if (cooldown) {
      startCooldown();
    }
  }

  private void destroyBlocks() {
    CollectionUtils.clearWhileIterating(blocks, FloatingEntity::destroy);
    CollectionUtils.clearWhileIterating(positionUpdaters, BukkitTask::cancel);
  }

  private static class GaiaTossProjectile extends BlockProjectile {
    private final boolean createParticles;

    public GaiaTossProjectile(Section config, AttackInfo attackInfo, boolean createParticles) {
      super(config, attackInfo);
      this.createParticles = createParticles;
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      entity.getWorld().playSound(entity.getLocation(), Sound.EXPLODE, 2, 1);
      new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(entity.getLocation());
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      Location loc = entity.getLocation();
      entity.getWorld().playSound(loc, Sound.IRONGOLEM_DEATH, 2, 0.5f);

      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
      new ParticleMaker(particle).boom(SSL.getInstance(), loc, 5, 0.5, 15);
    }

    @Override
    public void onTick() {
      if (createParticles) {
        for (int i = 0; i < 3; i++) {
          ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_NORMAL);
          new ParticleMaker(particle).show(entity.getLocation());
        }
      }
    }
  }
}
