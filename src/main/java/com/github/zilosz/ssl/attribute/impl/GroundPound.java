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
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class GroundPound extends RightClickAbility {
  @Nullable private BukkitTask fallTask;
  @Nullable private BukkitTask checkAirborneTask;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || fallTask != null;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 2, 1);

    player.setVelocity(new Vector(0, -config.getDouble("DownwardVelocity"), 0));
    double initialHeight = player.getLocation().getY();
    fallTask =
        Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> onRun(initialHeight), 0, 0);

    if (checkAirborneTask == null) {
      checkAirborneTask =
          Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), this::onGroundCheck, 0, 0);
    }
  }

  private void onRun(double initialHeight) {
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
    new ParticleMaker(particle).ring(EntityUtils.center(player), 90, 0, 1, 10);

    double fallen = Math.max(0, initialHeight - player.getLocation().getY());
    double maxFall = config.getDouble("MaxFall");
    double damage = YamlReader.incVal(config, "Damage", fallen, maxFall);
    double kb = YamlReader.incVal(config, "Kb", fallen, maxFall);

    boolean foundTarget = false;
    EntityFinder finder = new EntityFinder(new HitBoxSelector(config.getDouble("HitBox")));

    for (LivingEntity target : finder.findAll(player)) {
      Vector direction = player.getLocation().getDirection();
      Attack attack = YamlReader.attack(config, direction, getDisplayName());

      attack.getDamage().setDamage(damage);
      attack.getKb().setKb(kb);

      AttackInfo attackInfo = new AttackInfo(AttackType.GROUND_POUND, this);

      if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
        foundTarget = true;

        player.getWorld().playSound(target.getLocation(), Sound.EXPLODE, 2, 2);
        ParticleBuilder builder = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
        new ParticleMaker(builder).show(EntityUtils.top(target));
      }
    }

    if (foundTarget) {
      resetFall(false);
      kit.getJump().giveExtraJumps(1);

      double bounce = YamlReader.incVal(config, "Bounce", fallen, maxFall);
      player.setVelocity(new Vector(0, bounce, 0));
    }
  }

  private void resetFall(boolean stopGroundTask) {

    if (fallTask != null) {
      fallTask.cancel();
    }

    fallTask = null;

    if (stopGroundTask && checkAirborneTask != null) {
      checkAirborneTask.cancel();
      checkAirborneTask = null;
    }
  }

  private void onGroundCheck() {
    if (!EntityUtils.isPlayerGrounded(player)) return;

    if (fallTask != null) {
      player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_HIT, 2, 0.5f);
    }

    startCooldown();
    resetFall(true);
  }

  @Override
  public void deactivate() {
    super.deactivate();
    resetFall(true);
  }

  @EventHandler
  public void onKb(AttackEvent event) {
    if (event.getVictim() == player && fallTask != null) {
      event.getAttack().getKb().setDirection(null);
    }
  }
}
