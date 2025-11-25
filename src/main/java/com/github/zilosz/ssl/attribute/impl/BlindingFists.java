package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attack.KnockBack;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.math.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlindingFists extends PassiveAbility {
  private final Map<LivingEntity, Integer> chainCounts = new HashMap<>();
  private final Map<LivingEntity, BukkitTask> chainResetters = new HashMap<>();

  @Override
  public void deactivate() {
    super.deactivate();
    chainCounts.clear();
    player.removePotionEffect(PotionEffectType.SPEED);
    CollectionUtils.removeWhileIteratingOverValues(chainResetters, BukkitTask::cancel);
  }

  @Override
  public String getUseType() {
    return "Melee";
  }

  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (event.getEntity() == player && event.getDamager() instanceof LivingEntity) {
      resetChains();
    }
  }

  private void resetChains() {
    chainCounts.clear();
    player.removePotionEffect(PotionEffectType.SPEED);
  }

  @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true)
  public void onCustomDamage(AttackEvent event) {
    LivingEntity victim = event.getVictim();

    if (victim == player) {
      resetChains();
      return;
    }

    if (event.getInfo().getAttribute().getPlayer() != player) return;
    if (event.getInfo().getType() != AttackType.MELEE) return;

    event.getAttack().setName(getDisplayName());

    int maxChain = config.getInt("MaxChain");
    chainCounts.putIfAbsent(victim, 0);
    int currChain = chainCounts.get(victim);

    if (currChain > 0) {
      double pitch = MathUtils.incVal(0.5, 2, maxChain - 1, currChain - 1);
      player.playSound(player.getLocation(), Sound.ZOMBIE_REMEDY, 0.5f, (float) pitch);

      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
      Location center = EntityUtils.center(victim);
      new ParticleMaker(particle).boom(SSL.getInstance(), center, 1.5, 0.375, 5);

      for (int x = -1; x <= 1; x++) {

        for (int y = -1; y <= 2; y++) {

          for (int z = -1; z <= 1; z++) {
            Location location = victim.getLocation().clone().add(x, y, z);

            if (location.getBlock().getType() == Material.WEB) {
              location.getBlock().setType(Material.AIR);
              location.getWorld().playSound(location, Sound.ITEM_BREAK, 1, 1);
            }
          }
        }
      }

      if (currChain == 1) {
        new PotionEffectEvent(player, PotionEffectType.SPEED, 10_000, 1).apply();
      }
    }

    double maxDamageMul = config.getDouble("MaxDamageMultiplier");
    double damageMul = MathUtils.incVal(1, maxDamageMul, maxChain - 1, currChain);

    double maxKbMul = config.getDouble("MaxKbMultiplier");
    double kbMul = MathUtils.incVal(1, maxKbMul, maxChain - 1, currChain);

    double maxKbYMul = config.getDouble("MaxKbYMultiplier");
    double kbYMul = MathUtils.incVal(1, maxKbYMul, maxChain - 1, currChain);

    Attack settings = event.getAttack();
    settings.getDamage().setDamage(settings.getDamage().getDamage() * damageMul);

    KnockBack kbSettings = settings.getKb();
    kbSettings.setKb(kbSettings.getKb() * kbMul);
    kbSettings.setKbY(kbSettings.getKbY() * kbYMul);

    Optional.ofNullable(chainResetters.remove(victim)).ifPresent(BukkitTask::cancel);

    chainResetters.put(victim, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
      chainCounts.remove(victim);
      player.removePotionEffect(PotionEffectType.SPEED);
    }, config.getInt("ChainDuration")));

    if (currChain < maxChain - 1) {
      chainCounts.put(victim, chainCounts.get(victim) + 1);
    }
  }
}
