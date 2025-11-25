package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.NoteColor;

import java.util.List;

public class SpookyScream extends RightClickAbility {
  private int currRing;
  private double radius;
  private BukkitTask ringTask;
  private BukkitTask attackTask;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || currRing > 0;
  }

  @Override
  public void deactivate() {
    super.deactivate();
    resetActiveScream();
  }

  private void resetActiveScream() {
    currRing = 0;

    if (ringTask != null) {
      ringTask.cancel();
      attackTask.cancel();
    }
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    Location currLocation = player.getEyeLocation();

    player.getWorld().playSound(currLocation, Sound.WITHER_SHOOT, 1, 0.8f);
    player.getWorld().playSound(currLocation, Sound.GHAST_SCREAM, 0.5f, 2);

    Vector step = currLocation.getDirection().multiply(config.getDouble("RingGap"));

    radius = config.getDouble("Radius");
    List<Integer> notes = config.getIntList("Notes");

    ringTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      currLocation.add(step);

      NoteColor note = new NoteColor(CollectionUtils.selectRandom(notes));
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.NOTE).setParticleData(note);
      new ParticleMaker(particle).ring(currLocation, radius, config.getDouble("DegreeGap"));

      if (++currRing == config.getInt("Rings")) {
        resetActiveScream();
        startCooldown();

      }
      else {
        radius += config.getDouble("RadiusGrowthPerTick");
      }
    }, 0, config.getInt("TicksPerRing"));

    attackTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      double searchRadius = radius + config.getDouble("ExtraHitBox");

      new EntityFinder(new DistanceSelector(searchRadius))
          .findAll(player, currLocation)
          .forEach(target -> {
            boolean isPumpkin = target.hasMetadata("pumpkin");
            String attackPath = isPumpkin ? "PumpkinAttack" : "NormalAttack";
            Attack attack =
                YamlReader.attack(config.getSection(attackPath), step, getDisplayName());
            AttackInfo attackInfo = new AttackInfo(AttackType.SPOOKY_SCREAM, this);

            if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
              player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
              target.getWorld().playSound(target.getLocation(), Sound.WITHER_HURT, 1, 1);

              PotionEffect effect = YamlReader.potionEffect(config.getSection("Wither"));
              new PotionEffectEvent(target, effect).apply();

              if (isPumpkin) {
                target.getWorld().playSound(target.getLocation(), Sound.WITHER_HURT, 1, 1);

                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
                new ParticleMaker(particle).show(target.getLocation());
              }
            }
          });
    }, 0, 0);
  }
}
