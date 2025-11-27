package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.MathUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class VoltTackle extends RightClickAbility {
  private final Map<Item, BukkitTask> particles = new HashMap<>();
  private int ticksMoving = -1;
  private BukkitTask moveTask;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || ticksMoving > -1;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1.5f);
    EntitySelector selector = new HitBoxSelector(config.getInt("HitBox"));

    int duration = config.getInt("DurationTicks");

    moveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (++ticksMoving >= duration) {
        reset();
        startCooldown();
        playEndSound();
        return;
      }

      Location eyeLoc = player.getEyeLocation();
      double speed = YamlReader.incVal(config, "Velocity", ticksMoving, duration);
      Vector velocity = eyeLoc.getDirection().multiply(speed);

      if (Math.abs(velocity.getY()) > config.getDouble("MaxVelocityY")) {
        velocity.setY(Math.signum(velocity.getY()) * config.getDouble("MaxVelocityY"));
      }

      player.setVelocity(velocity);

      for (int i = 0; i < config.getInt("ParticlesPerTick"); i++) {
        Location center = EntityUtils.center(player);
        Item gold = player.getWorld().dropItem(center, new ItemStack(Material.GOLD_INGOT));
        gold.setPickupDelay(Integer.MAX_VALUE);
        gold.setVelocity(VectorUtils
            .randomVector(null)
            .multiply(config.getDouble("ParticleSpeed")));

        int particleDuration = config.getInt("ParticleDuration");

        particles.put(
            gold,
            Bukkit.getScheduler().runTaskLater(SSL.getInstance(), gold::remove, particleDuration)
        );
      }

      float pitch = (float) MathUtils.incVal(0.5, 2, duration, ticksMoving);
      player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1, pitch);

      new EntityFinder(selector).findClosest(player).ifPresent(target -> {
        double damage = YamlReader.incVal(config, "Damage", ticksMoving, duration);
        double kb = YamlReader.incVal(config, "Kb", ticksMoving, duration);

        Attack attack = YamlReader.attack(config, velocity, getDisplayName());
        attack.getDamage().setDamage(damage);
        attack.getKb().setKb(kb);

        AttackInfo attackInfo = new AttackInfo(AttackType.VOLT_TACKLE, this);

        if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
          player.getWorld().playSound(player.getLocation(), Sound.FALL_BIG, 1, 2);
          player.getWorld().strikeLightningEffect(target.getLocation());

          Section settings = config.getSection("Recoil");
          double recoilDamage = YamlReader.incVal(settings, "Damage", ticksMoving, duration);
          double recoilKb = YamlReader.incVal(settings, "Kb", ticksMoving, duration);

          Attack recoil = YamlReader.attack(settings, velocity.multiply(-1), getDisplayName());
          recoil.getDamage().setDamage(recoilDamage);
          recoil.getKb().setKb(recoilKb);

          AttackInfo info = new AttackInfo(AttackType.VOLT_TACKLE_RECOIL, this);
          SSL.getInstance().getDamageManager().attack(player, recoil, info);
        }

        reset();
        startCooldown();
      });
    }, 0, 0);
  }

  private void reset() {
    if (ticksMoving == -1) return;

    moveTask.cancel();
    ticksMoving = -1;

    CollectionUtils.clearOverEntries(particles, Item::remove, BukkitTask::cancel);
  }

  private void playEndSound() {
    player.playSound(player.getLocation(), Sound.WOLF_DEATH, 1, 0.5f);
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    if (event.getPlayer() == player && !player.isSneaking()) {
      reset();
      startCooldown();
      playEndSound();
    }
  }
}
