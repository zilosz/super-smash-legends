package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Bow;
import com.github.zilosz.ssl.projectile.ArrowProjectile;
import com.github.zilosz.ssl.util.RunnableUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Barrage extends Bow {
  private int stage = 1;
  private BukkitTask stageTask;

  @Override
  public void onStart() {

    stageTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      if (stage > getStages()) return;

      player.setExp((float) stage / getStages());

      float pitch = (float) MathUtils.incVal(0.5, 2, getStages(), stage - 1);
      player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 2, pitch);

      if (stage == getStages()) {
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.7f, 0.5f);
      }

      stage++;
    }, config.getInt("StageTicks"), config.getInt("StageTicks"));
  }

  private int getStages() {
    return config.getInt("Stages");
  }

  @Override
  public void onShot(double force) {
    launch(force, true);
    int arrowCount = config.getInt("MaxArrowCount");
    int amount = (int) MathUtils.incVal(1, arrowCount, getStages(), stage - 1);
    int interval = config.getInt("TicksBetweenShot");

    RunnableUtils.runIntervaledTask(SSL.getInstance(),
        amount - 1,
        interval,
        () -> launch(force, false)
    );
  }

  private void launch(double force, boolean first) {
    BarrageArrow arrow = new BarrageArrow(config, new AttackInfo(AttackType.BARRAGE, this));
    arrow.setSpeed(force * config.getDouble("MaxSpeed"));

    if (first) {
      arrow.setSpread(0);
    }

    arrow.launch();
  }

  @Override
  public void onFinish() {
    stage = 1;
    player.setExp(0);

    if (stageTask != null) {
      stageTask.cancel();
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    onFinish();
  }

  private static class BarrageArrow extends ArrowProjectile {

    public BarrageArrow(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onLaunch() {
      launcher.getWorld().playSound(launcher.getLocation(), Sound.SHOOT_ARROW, 1, 2);
    }

    @Override
    public void onTick() {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_NORMAL)).show(entity.getLocation());
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      ParticleBuilder particle =
          new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(200, 200, 200));
      new ParticleMaker(particle).boom(SSL.getInstance(), entity.getLocation(), 1.2, 0.4, 6);
    }
  }
}
