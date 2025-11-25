package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.effects.Effects;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Berserk extends RightClickAbility {
  private boolean active;
  private BukkitTask resetTask;
  private Firework firework;
  private BukkitTask particleTask;
  private int ogJumps;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || active;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    active = true;

    ogJumps = kit.getJump().getMaxCount();
    kit.getJump().setMaxCount(ogJumps + config.getInt("ExtraJumps"));

    int speed = config.getInt("Speed");
    new PotionEffectEvent(player, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();

    FireworkEffect.Builder settings = FireworkEffect
        .builder()
        .withColor(kit.getColor().getBukkitColor())
        .with(FireworkEffect.Type.BURST)
        .trail(true);

    firework = Effects.launchFirework(player.getLocation(), settings, 1);

    particleTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      Location loc = player.getLocation().add(0, 0.3, 0);
      new ParticleMaker(new ParticleBuilder(ParticleEffect.REDSTONE)).ring(loc, 90, 0, 0.5, 20);
    }, 0, 5);

    player.playSound(player.getLocation(), Sound.WOLF_GROWL, 1, 1);

    resetTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
      reset();
      startCooldown();
    }, config.getInt("Duration"));
  }

  public void reset() {
    if (!active) return;

    active = false;

    kit.getJump().setMaxCount(ogJumps);
    player.removePotionEffect(PotionEffectType.SPEED);

    firework.remove();
    particleTask.cancel();
    resetTask.cancel();

    player.playSound(player.getLocation(), Sound.WOLF_WHINE, 1, 1);
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @EventHandler
  public void onAttack(AttackEvent event) {
    if (event.getInfo().getAttribute().getPlayer() != player) return;
    if (!active) return;

    double multiplier = config.getDouble("DamageMultiplier");
    event.getDamage().setDamage(event.getDamage().getDamage() * multiplier);

    if (event.getInfo().getType() == AttackType.MELEE) {
      event.getAttack().setName(getDisplayName());
    }
  }
}
