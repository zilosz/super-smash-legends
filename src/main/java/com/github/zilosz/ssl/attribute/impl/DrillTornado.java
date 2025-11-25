package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class DrillTornado extends RightClickAbility {
  private BukkitTask prepareTask;
  private float pitch = 0.5f;
  private int ticksPreparing;
  private boolean isDrilling;
  private BukkitTask drillTask;
  private BukkitTask drillCancelTask;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || ticksPreparing > 0 || isDrilling;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    int prepareDuration = config.getInt("PrepareTicks");

    prepareTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      player.setVelocity(new Vector(0, 0.03, 0));
      player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 1, pitch);

      if (ticksPreparing % 2 == 0) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK);
        new ParticleMaker(particle).hollowSphere(EntityUtils.center(player), 1, 20);
      }

      if (ticksPreparing++ < prepareDuration) {
        pitch += 1.5f / prepareDuration;
        return;
      }

      prepareTask.cancel();
      ticksPreparing = 0;
      isDrilling = true;

      drillTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
        double velocity = config.getDouble("Velocity");
        player.setVelocity(player.getEyeLocation().getDirection().multiply(velocity));

        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);

        for (double y = 0; y < 2 * Math.PI; y += config.getDouble("ParticleGap")) {
          Location particleLoc = player.getLocation().add(1.5 * Math.cos(y), y, 1.5 * Math.sin(y));
          new ParticleMaker(new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK)).show(particleLoc);
        }

        EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(player).forEach(target -> {
          Attack attack = YamlReader.attack(config, player.getVelocity(), getDisplayName());
          AttackInfo attackInfo = new AttackInfo(AttackType.DRILL_TORNADO, this);

          if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
            player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 0.5f);
          }
        });
      }, 0, 0);

      drillCancelTask = Bukkit
          .getScheduler()
          .runTaskLater(SSL.getInstance(), () -> reset(true), config.getInt("Duration"));
    }, 0, 0);
  }

  private void reset(boolean natural) {
    pitch = 0.5f;
    ticksPreparing = 0;
    isDrilling = false;

    if (natural) {
      player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 1.5f);
      startCooldown();
    }

    if (prepareTask != null) {
      prepareTask.cancel();
    }

    if (drillTask != null) {
      drillTask.cancel();
      drillCancelTask.cancel();
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset(false);
  }

  @EventHandler
  public void onDamage(AttackEvent event) {
    if (event.getVictim() == player && ticksPreparing > 0) {
      reset(true);
    }
  }
}
