package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.block.BlockUtils;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.FloatingEntity;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.List;

public class ChainOfSteel extends RightClickAbility {
  private final List<FloatingEntity<FallingBlock>> chainBlocks = new ArrayList<>();
  private Vector direction;
  private BukkitTask chainTask;
  private int chainTicks;
  private BukkitTask pullTask;
  private BukkitTask pullSoundTask;
  private BukkitTask effectRemoveTask;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || chainTicks > 0;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    Location currLocation = EntityUtils.center(player);
    direction = player.getEyeLocation().getDirection();
    Vector step = direction.clone().multiply(config.getDouble("ChainSpeed"));

    EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

    chainTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      player.setVelocity(new Vector(0, 0.03, 0));
      currLocation.add(step);

      if (chainTicks >= config.getInt("ChainTicks")) {
        reset(true);
        return;
      }

      if (chainTicks % 2 == 0) {
        FallingBlock block = BlockUtils.spawnFallingBlock(currLocation, Material.IRON_FENCE);
        chainBlocks.add(FloatingEntity.fromEntity(block));
      }

      player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 1, 2);

      if (currLocation.getBlock().getType().isSolid()) {
        pullTowardsLocation(currLocation);
      }
      else {
        new EntityFinder(selector).findClosest(player, currLocation).ifPresent(target -> {
          Attack attack = YamlReader.attack(config, null, getDisplayName());
          AttackInfo attackInfo = new AttackInfo(AttackType.CHAIN_OF_STEEL, this);

          if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
            player.getWorld().playSound(currLocation, Sound.EXPLODE, 1, 1.5f);
            new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(currLocation);
            pullTowardsLocation(currLocation);
          }
        });
      }

      chainTicks++;
    }, 0, 0);
  }

  private void reset(boolean cooldown) {
    chainTicks = 0;
    chainTask.cancel();

    CollectionUtils.clearWhileIterating(chainBlocks, FloatingEntity::destroy);

    if (pullTask != null) {
      pullTask.cancel();
      pullSoundTask.cancel();
      effectRemoveTask.cancel();
    }

    if (cooldown) {
      startCooldown();
    }
  }

  private void pullTowardsLocation(Location location) {
    chainTask.cancel();

    player.getWorld().playSound(location, Sound.IRONGOLEM_HIT, 1, 0.5f);

    Vector pullVelocity = direction.clone().multiply(config.getDouble("PullSpeed"));
    pullTask = Bukkit
        .getScheduler()
        .runTaskTimer(SSL.getInstance(), () -> player.setVelocity(pullVelocity), 0, 0);

    pullSoundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      player.getWorld().playSound(player.getLocation(), Sound.STEP_LADDER, 2, 1);
    }, 0, 0);

    double distance = location.distance(player.getLocation());
    double travelTicks = distance / config.getInt("PullSpeed");
    int ticksPerRemoval = (int) (travelTicks / chainBlocks.size());

    effectRemoveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      if (chainBlocks.isEmpty()) {
        reset(true);
      }
      else {
        chainBlocks.remove(0).destroy();
      }
    }, ticksPerRemoval, ticksPerRemoval);
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (chainTicks > 0) {
      reset(false);
    }
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    if (event.getPlayer() == player && chainTicks > 0) {
      reset(true);
      player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 2);
    }
  }

  @EventHandler
  public void onEntityBlockChange(EntityChangeBlockEvent event) {
    if (chainBlocks.stream().anyMatch(floating -> floating.getEntity() == event.getEntity())) {
      event.setCancelled(true);
    }
  }
}
