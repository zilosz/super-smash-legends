package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.event.attribute.EnergyEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.message.Chat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.List;
import java.util.stream.Collectors;

public class GoldRush extends PassiveAbility {
  private List<Material> passableMaterials;
  private boolean isMining;
  private BukkitTask resetTask;
  private BukkitTask moveTask;
  private BukkitTask particleTask;

  @Override
  public void activate() {
    super.activate();
    List<String> strings = config.getStringList("PassableMaterials");
    passableMaterials = strings.stream().map(Material::valueOf).collect(Collectors.toList());
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @Override
  public String getUseType() {
    return "Sneak";
  }

  private void reset() {
    if (!isMining) return;

    isMining = false;

    resetTask.cancel();
    moveTask.cancel();
    particleTask.cancel();

    Location location = player.getLocation();

    while (!isPassable(location) && !isPassable(location.add(0, 1, 0))) {
      location.add(0, 1, 0);
    }

    player.getWorld().playSound(player.getLocation(), Sound.DIG_GRASS, 2, 0.5f);
    player.teleport(location);
    player.setGameMode(GameMode.SURVIVAL);
    player.removePotionEffect(PotionEffectType.BLINDNESS);

    SSL.getInstance().getDamageManager().showEntityIndicator(player);

    double velocity = config.getDouble("EmergeVelocity");
    double velY = config.getDouble("EmergeVelocityY");
    player.setVelocity(location.getDirection().multiply(velocity).setY(velY));

    kit.getJump().replenish();
  }

  private boolean isPassable(Location location) {
    return passableMaterials.contains(location.getBlock().getType());
  }

  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    if (event.getPlayer() != player) return;
    if (player.isSneaking()) return;

    if (isMining) {
      reset();
      return;
    }

    if (player.getExp() < 1) return;

    if (!EntityUtils.isPlayerGrounded(player)) {
      Chat.ABILITY.send(player, "&7You must be grounded to mine.");
      return;
    }

    startMining();
  }

  private void startMining() {
    isMining = true;

    player.getWorld().playSound(player.getLocation(), Sound.DIG_GRASS, 2, 0.85f);

    player.setExp(0);
    player.setGameMode(GameMode.SPECTATOR);
    teleport(player.getLocation());

    SSL.getInstance().getDamageManager().hideEntityIndicator(player);

    int blindness = config.getInt("Blindness");
    new PotionEffectEvent(player, PotionEffectType.BLINDNESS, 10_000, blindness).apply();

    resetTask = Bukkit
        .getScheduler()
        .runTaskLater(SSL.getInstance(), this::reset, config.getInt("MaxTicks"));

    moveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      Location location = player.getLocation();
      Location body = location.clone().add(0, 1, 0);
      Location eyes = body.clone().add(0, 1, 0);
      Location aboveEyes = eyes.clone().add(0, 1, 0);

      if (!isPassable(eyes) && isPassable(aboveEyes)) {
        teleport(aboveEyes);
      }
      else if (isPassable(eyes) && isPassable(body) && !isPassable(location)) {
        teleport(body);
      }
      else if (isPassable(body) && isPassable(eyes)) {
        reset();
        return;
      }

      double velocity = config.getDouble("Velocity");
      player.setVelocity(player.getEyeLocation().getDirection().multiply(velocity).setY(0));

      player.getWorld().playSound(player.getLocation(), Sound.DIG_STONE, 2, 1.5f);
    }, 0, 0);

    int ticksPerParticle = config.getInt("TicksPerParticle");

    particleTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      Location location = player.getLocation().add(0, 2, 0);

      while (!isPassable(location)) {
        location.add(0, 1, 0);
      }

      for (int i = 0; i < 5; i++) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_NORMAL);
        new ParticleMaker(particle).setSpread(0.75, 0, 0.75).show(location);
      }
    }, ticksPerParticle, ticksPerParticle);
  }

  private void teleport(Location location) {
    player.teleport(location.subtract(0, config.getDouble("Depth"), 0));
  }

  @EventHandler
  public void onAbilityUse(AbilityUseEvent event) {
    if (event.getAbility().getPlayer() == player && isMining) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onJump(DoubleJumpEvent event) {
    if (event.getPlayer() == player && isMining) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEnergy(EnergyEvent event) {
    if (event.getPlayer() == player && isMining) {
      event.setEnergy(0);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() == player &&
        event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
      event.setCancelled(true);
    }
  }
}
