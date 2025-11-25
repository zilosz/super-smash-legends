package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class OlympicDive extends RightClickAbility {
  private BukkitTask task;
  private BukkitTask diveDelayer;
  private boolean canDive;
  private State diveState = State.INACTIVE;

  @Override
  public void onClick(PlayerInteractEvent event) {

    switch (diveState) {

      case INACTIVE:
        ascend();
        break;

      case ASCENDING:
        dive();
        break;
    }
  }

  private void ascend() {
    sendUseMessage();

    diveState = State.ASCENDING;

    player.setVelocity(new Vector(0, config.getDouble("AscendVelocity"), 0));
    player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 0.5f, 2);

    EntitySelector selector = new DistanceSelector(config.getDouble("PullDistance"));
    EntityFinder finder = new EntityFinder(selector).setTeamPreference(TeamPreference.ANY);

    finder.findAll(player).forEach(target -> {
      Vector pullDirection = VectorUtils.fromTo(target, player).normalize();
      Vector extraY = new Vector(0, config.getDouble("ExtraPullY"), 0);
      Vector velocity = pullDirection.multiply(config.getDouble("PullVelocity")).add(extraY);
      velocity.setY(Math.max(velocity.getY(), config.getDouble("MaxPullY")));
      target.setVelocity(velocity);
    });

    int diveDelay = config.getInt("DiveDelay");
    diveDelayer =
        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> canDive = true, diveDelay);

    task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (EntityUtils.isPlayerGrounded(player)) {

        switch (diveState) {

          case ASCENDING:
            reset(true);
            break;

          case DIVING:
            reset(true);
            onDiveFinish();
        }

      }
      else if (diveState == State.ASCENDING) {

        for (int i = 0; i < 10; i++) {
          ParticleBuilder particle = new ParticleBuilder(ParticleEffect.DRIP_WATER);
          new ParticleMaker(particle).setSpread(0.5).show(player.getLocation());
        }
      }
    }, 4, 0);
  }

  private void reset(boolean natural) {
    if (diveState == State.INACTIVE) return;

    diveState = State.INACTIVE;
    task.cancel();
    canDive = false;

    if (diveDelayer != null) {
      diveDelayer.cancel();
    }

    if (natural) {
      player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 0.5f, 2);
      startCooldown();
    }
  }

  private void onDiveFinish() {
    player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 1, 1);
    player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.5f, 2);

    for (int i = 0; i < 10; i++) {
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
      new ParticleMaker(particle).setSpread(3.5, 0.6, 3.5).show(player.getLocation());
    }

    double radius = config.getDouble("DiveDamageRadius");
    EntitySelector selector = new DistanceSelector(radius);

    new EntityFinder(selector).findAll(player).forEach(target -> {
      double distance = target.getLocation().distance(player.getLocation());
      double damage = YamlReader.decreasingValue(config, "DiveDamage", distance, radius);
      double kb = YamlReader.decreasingValue(config, "DiveKb", distance, radius);

      Vector direction = VectorUtils.fromTo(player, target);
      Attack attack = YamlReader.attack(config, direction, getDisplayName());
      attack.getDamage().setDamage(damage);
      attack.getKb().setKb(kb);

      AttackInfo attackInfo = new AttackInfo(AttackType.OLYMPIC_DIVE, this);
      SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
    });
  }

  private void dive() {
    if (!canDive) return;

    diveState = State.DIVING;

    double diveVelocity = config.getDouble("DiveVelocity");
    player.setVelocity(player.getEyeLocation().getDirection().multiply(diveVelocity));

    player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 3, 0.5f);
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset(false);
  }

  @EventHandler
  public void onAttack(AttackEvent event) {
    if (event.getInfo().getAttribute().getPlayer() == player && diveState != State.INACTIVE) {
      event.getAttack().getKb().setDirection(null);
    }
  }

  private enum State {
    INACTIVE, ASCENDING, DIVING
  }
}
