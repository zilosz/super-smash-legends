package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.DisguiseUtils;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.Collection;

public class SquidDash extends RightClickAbility {
  private final Collection<Item> particles = new ArrayList<>();
  private Vector velocity;
  private BukkitTask dashTask;
  private int ticksDashing = -1;
  private BukkitTask invisibilityTask;
  private boolean invisible;

  private int getMaxDashTicks() {
    return config.getInt("MaxTicks");
  }

  private void stopDash() {
    Location center = EntityUtils.center(player);

    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
    new ParticleMaker(particle).solidSphere(center, 1.5, 7, 0.5);

    new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)
        .setOffset(0.6f, 0.6f, 0.6f)
        .setLocation(center)
        .display();

    player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 2, 0.5f);
    player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1, 1);

    double damage = YamlReader.incVal(config, "Damage", ticksDashing, getMaxDashTicks());
    double kb = YamlReader.incVal(config, "Kb", ticksDashing, getMaxDashTicks());

    EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

    new EntityFinder(selector).findAll(player).forEach(target -> {
      Vector direction = VectorUtils.fromTo(player, target);
      Attack attack = YamlReader.attack(config, direction, getDisplayName());
      attack.getDamage().setDamage(damage);
      attack.getKb().setKb(kb);

      AttackInfo attackInfo = new AttackInfo(AttackType.SQUID_DASH, this);
      SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
    });

    invisible = true;
    SSL.getInstance().getDamageManager().hideEntityIndicator(player);
    Bukkit.getOnlinePlayers().forEach(other -> other.hidePlayer(player));

    int ticks =
        (int) YamlReader.incVal(config, "InvisibilityTicks", ticksDashing, getMaxDashTicks());

    invisibilityTask =
        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), this::unHidePlayer, ticks);

    resetDash();
    player.setVelocity(velocity);

    startCooldown();
  }

  private void startDash() {
    sendUseMessage();

    Vector direction = player.getLocation().getDirection().setY(0).normalize();
    velocity = direction.multiply(config.getDouble("Velocity"));

    Disguise disguise =
        DisguiseUtils.applyDisguiseParams(player, new MobDisguise(DisguiseType.SQUID));
    DisguiseAPI.disguiseToAll(player, disguise);

    dashTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (++ticksDashing >= getMaxDashTicks()) {
        stopDash();
        return;
      }

      player.setVelocity(velocity);
      player.getWorld().playSound(player.getLocation(), Sound.SPLASH2, 1, 1);

      Section particleConfig = config.getSection("Particle");

      Location particleLoc = EntityUtils.center(player);
      particleLoc.setDirection(velocity.clone().normalize());

      double spread = particleConfig.getDouble("Spread");
      double speed = particleConfig.getDouble("Speed");

      int duration = particleConfig.getInt("Duration");

      for (int i = 0; i < particleConfig.getInt("CountPerTick"); i++) {
        Item particle = player.getWorld().dropItem(particleLoc, new ItemStack(Material.INK_SACK));
        particle.setPickupDelay(Integer.MAX_VALUE);

        particle.setVelocity(VectorUtils
            .randomVectorInDirection(particleLoc, spread)
            .multiply(speed));

        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), particle::remove, duration);
        particles.add(particle);
      }
    }, 0, 0);
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    if (ticksDashing == -1) {
      startDash();
    }
    else {
      stopDash();
    }
  }

  @Override
  public void deactivate() {
    reset();
    super.deactivate();
  }

  private void reset() {

    if (invisible) {
      unHidePlayer();
    }

    resetDash();

    particles.forEach(Item::remove);
    particles.clear();
  }

  private void unHidePlayer() {
    invisibilityTask.cancel();
    invisible = false;

    SSL.getInstance().getDamageManager().showEntityIndicator(player);
    Bukkit.getOnlinePlayers().forEach(other -> other.showPlayer(player));

    player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 1, 2);

    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
    new ParticleMaker(particle).solidSphere(EntityUtils.center(player), 1, 5, 0.5);
  }

  private void resetDash() {
    if (ticksDashing == -1) return;

    ticksDashing = -1;
    dashTask.cancel();

    DisguiseAPI.undisguiseToAll(player);
  }

  @EventHandler
  public void onDamage(DamageEvent event) {
    if (event.getVictim() == player && invisible) {
      unHidePlayer();
    }
  }

  @EventHandler
  public void onAttack(AttackEvent event) {
    if (event.getVictim() != player) return;

    if (event.getInfo().getType() == AttackType.MELEE) {

      if (ticksDashing > -1) {
        event.setCancelled(true);
      }
    }
    else {

      if (ticksDashing > 0) {
        startCooldown();
      }

      reset();
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (invisible) {
      event.getPlayer().hidePlayer(player);
    }
  }
}
