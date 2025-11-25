package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Rasengan extends RightClickAbility {
  private BukkitTask mainTask;
  private BukkitTask cancelTask;
  private boolean leapt;
  private boolean active;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || active;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    active = true;
    hotbarItem.hide();

    start(player);
    Bukkit.getPluginManager().callEvent(new RasenganStartEvent(this));

    mainTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      display(player);
      Bukkit.getPluginManager().callEvent(new RasenganDisplayEvent(this));
    }, 0, 0);

    int lifespan = config.getInt("Lifespan");
    cancelTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), this::reset, lifespan);
  }

  public void start(Player entity) {
    entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 0.5f, 1);
    int speed = config.getInt("Speed");
    new PotionEffectEvent(entity, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();
  }

  public void display(Entity entity) {
    ParticleBuilder particle =
        new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(173, 216, 230));
    new ParticleMaker(particle).hollowSphere(EntityUtils.underHand(entity, 0), 0.15, 20);
  }

  private void reset() {
    if (!active) return;

    leapt = false;
    active = false;

    mainTask.cancel();
    cancelTask.cancel();

    hotbarItem.show();

    end(player);
    Bukkit.getPluginManager().callEvent(new RasenganEndEvent(this));

    startCooldown();
  }

  public void end(LivingEntity entity) {
    entity.getWorld().playSound(entity.getLocation(), Sound.BLAZE_HIT, 2, 1);
    entity.removePotionEffect(PotionEffectType.SPEED);
  }

  @Override
  public void deactivate() {
    reset();
    super.deactivate();
  }

  @EventHandler
  public void onHandSwitch(PlayerItemHeldEvent event) {
    if (event.getPlayer() == player && event.getNewSlot() != slot && active) {
      reset();
    }
  }

  @EventHandler
  public void onMelee(AttackEvent event) {
    if (event.getInfo().getAttribute().getPlayer() != player) return;
    if (event.getInfo().getType() != AttackType.MELEE) return;
    if (!active) return;

    modifyMeleeAttack(event.getAttack(), config);
    displayAttack(event.getVictim());

    reset();
  }

  public static void modifyMeleeAttack(Attack attack, Section config) {
    attack.getDamage().setDamage(config.getDouble("Damage"));
    attack.getKb().setKb(config.getDouble("Kb"));
    attack.getKb().setKbY(config.getDouble("KbY"));
    attack.getKb().setFactorsHealth(config.getBoolean("FactorsHealth"));
  }

  public static void displayAttack(LivingEntity victim) {
    victim.getWorld().playSound(victim.getLocation(), Sound.EXPLODE, 2, 1);

    for (int i = 0; i < 3; i++) {
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
      new ParticleMaker(particle).setSpread(0.4f).show(EntityUtils.center(victim));
    }
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    if (event.getPlayer() != player) return;
    if (!active) return;
    if (leapt) return;

    leapt = true;

    leap(player);
    Bukkit.getPluginManager().callEvent(new RasenganLeapEvent(this));
  }

  public void leap(LivingEntity entity) {
    double velocity = config.getDouble("LeapVelocity");
    entity.setVelocity(entity.getEyeLocation().getDirection().multiply(velocity));
    entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_IDLE, 0.5f, 2);
  }

  @Getter
  @RequiredArgsConstructor
  public abstract static class RasenganEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Rasengan rasengan;

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }

  public static class RasenganLeapEvent extends RasenganEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public RasenganLeapEvent(Rasengan rasengan) {
      super(rasengan);
    }

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }

  public static class RasenganEndEvent extends RasenganEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public RasenganEndEvent(Rasengan rasengan) {
      super(rasengan);
    }

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }

  public static class RasenganDisplayEvent extends RasenganEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public RasenganDisplayEvent(Rasengan rasengan) {
      super(rasengan);
    }

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }

  public static class RasenganStartEvent extends RasenganEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public RasenganStartEvent(Rasengan rasengan) {
      super(rasengan);
    }

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }
}
