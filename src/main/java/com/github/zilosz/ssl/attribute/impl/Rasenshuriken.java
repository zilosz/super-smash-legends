package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Rasenshuriken extends RightClickAbility {
  private BukkitTask task;
  private int ticksCharged = -1;

  @Override
  public void onClick(PlayerInteractEvent event) {
    player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
    hotbarItem.hide();

    task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (++ticksCharged >= config.getInt("Lifespan")) {
        reset();
        startCooldown();
        return;
      }

      if (ticksCharged % 2 == 0) {
        displayOnHead(player);
        Bukkit.getPluginManager().callEvent(new RasenshurikenDisplayEvent(this));
      }

      if (ticksCharged % 7 == 0) {
        player.getWorld().playSound(player.getLocation(), Sound.FUSE, 1, 1);
      }
    }, 0, 0);
  }

  private void reset() {
    if (ticksCharged == -1) return;

    ticksCharged = -1;
    task.cancel();
    hotbarItem.show();

    player.getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 3, 2);
  }

  public void displayOnHead(Player entity) {
    display(getHeadLocation(entity), false);
  }

  public static void display(Location location, boolean tilted) {
    float pitch = 90;
    float yaw = location.getYaw();

    if (tilted) {
      float actual = location.getPitch();
      pitch = actual >= 0 ? actual - 90 : actual + 90;
    }

    for (double radius = 0; radius <= 0.5; radius += 0.16) {
      ParticleBuilder outerParticle =
          new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(255, 255, 255));
      new ParticleMaker(outerParticle).ring(location, pitch, yaw, radius, 18);
    }

    ParticleBuilder innerParticle =
        new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(173, 216, 230));
    new ParticleMaker(innerParticle).hollowSphere(location, 0.3, 5);
  }

  private Location getHeadLocation(Entity entity) {
    return EntityUtils.top(entity).add(0, config.getDouble("Height"), 0);
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @EventHandler
  public void onPlayerAnimation(PlayerAnimationEvent event) {
    if (event.getPlayer() != player) return;
    if (ticksCharged == -1) return;

    Bukkit.getPluginManager().callEvent(new RasenshurikenLaunchEvent(this));
    launch(player, this, AttackType.RASENSHURIKEN);

    reset();
    startCooldown();
  }

  public Shuriken launch(Player entity, Ability owningAbility, AttackType attackType) {
    Section config = this.config.getSection("Projectile");
    Shuriken shuriken = new Shuriken(config, new AttackInfo(attackType, owningAbility));
    Vector direction = entity.getEyeLocation().getDirection();
    shuriken.setOverrideLocation(getHeadLocation(entity).setDirection(direction));
    shuriken.launch();
    return shuriken;
  }

  @EventHandler
  public void onHandSwitch(PlayerItemHeldEvent event) {
    if (event.getPlayer() == player && event.getNewSlot() != slot && ticksCharged > -1) {
      reset();
      startCooldown();
    }
  }

  public static class Shuriken extends ItemProjectile {

    public Shuriken(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
      attack.getDamage().setDamage(config.getDouble("MaxDamage"));
      attack.getKb().setKb(config.getDouble("MaxKb"));
    }

    @Override
    public void onLaunch() {
      entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_IDLE, 0.5f, 1);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      onHit(null);
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      onHit(target);
    }

    @Override
    public void onTick() {

      if (ticksAlive % 2 == 0) {
        display(entity.getLocation(), true);
      }

      if (ticksAlive % 5 == 0) {
        entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_SHOOT, 0.5f, 1);
      }
    }

    private void onHit(LivingEntity avoid) {
      Location loc = entity.getLocation();

      double radius = config.getDouble("Radius");

      entity.getWorld().playSound(loc, Sound.EXPLODE, 1.5f, 1);

      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
      new ParticleMaker(particle).solidSphere(loc, radius / 2, 40, 0.1);

      EntityFinder finder = new EntityFinder(new DistanceSelector(radius));

      if (avoid != null) {
        finder.avoid(avoid);
      }

      finder.findAll(launcher, loc).forEach(target -> {
        double distanceSq = target.getLocation().distanceSquared(loc);
        double damage = YamlReader.decreasingValue(config, "Damage", distanceSq, radius * radius);
        double kb = YamlReader.decreasingValue(config, "Kb", distanceSq, radius * radius);

        Vector direction = VectorUtils.fromTo(entity, target);
        String name = ((Ability) attackInfo.getAttribute()).getDisplayName();
        Attack attack = YamlReader.attack(config, direction, name);
        attack.getDamage().setDamage(damage);
        attack.getKb().setKb(kb);

        SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
      });
    }
  }

  @Getter
  @RequiredArgsConstructor
  private static class RasenshurikenEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Rasenshuriken rasenshuriken;

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }

  public static class RasenshurikenDisplayEvent extends RasenshurikenEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public RasenshurikenDisplayEvent(Rasenshuriken rasenshuriken) {
      super(rasenshuriken);
    }

    public static HandlerList getHandlerList() {
      return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
      return HANDLERS;
    }
  }

  public static class RasenshurikenLaunchEvent extends RasenshurikenEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public RasenshurikenLaunchEvent(Rasenshuriken rasenshuriken) {
      super(rasenshuriken);
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
