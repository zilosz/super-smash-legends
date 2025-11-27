package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.game.InGameProfile;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class AttackManager {
  private final Map<LivingEntity, DamageIndicator> indicators = new HashMap<>();
  private final Map<LivingEntity, BukkitTask> indicatorRemovers = new HashMap<>();
  private final Collection<LivingEntity> entitiesWithInvisIndic = new HashSet<>();

  private final Map<Player, Double> playerComboDamages = new HashMap<>();
  private final Map<Player, BukkitTask> comboDamageRemovers = new HashMap<>();

  private final Map<LivingEntity, AttackSource> lastDamageSources = new HashMap<>();
  private final Map<LivingEntity, BukkitTask> lastDamageRemovers = new HashMap<>();

  private final Map<LivingEntity, Map<AttackInfo, BukkitTask>> immunities = new HashMap<>();

  public AttackSource getLastAttackSource(LivingEntity entity) {
    return lastDamageSources.get(entity);
  }

  public void updateIndicator(LivingEntity entity, double damage) {
    DamageIndicator indicator;

    if (indicators.containsKey(entity)) {
      indicator = indicators.get(entity);
      indicatorRemovers.remove(entity).cancel();
      indicator.updateDamage(damage);
    }
    else {
      indicator = DamageIndicator.create(entity, damage);
      indicators.put(entity, indicator);

      if (entitiesWithInvisIndic.contains(entity)) {
        indicator.setVisibility(VisibilitySettings.Visibility.HIDDEN);
      }
    }

    indicatorRemovers.put(entity, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
      clearIndicator(entity);
    }, getComboDuration()));
  }

  public void clearIndicator(LivingEntity entity) {
    Optional.ofNullable(indicators.remove(entity)).ifPresent(DamageIndicator::destroy);
    Optional.ofNullable(indicatorRemovers.remove(entity)).ifPresent(BukkitTask::cancel);
  }

  private int getComboDuration() {
    return SSL.getInstance().getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
  }

  public void hideEntityIndicator(LivingEntity entity) {
    entitiesWithInvisIndic.add(entity);

    Optional
        .ofNullable(indicators.get(entity))
        .ifPresent(ind -> ind.setVisibility(VisibilitySettings.Visibility.HIDDEN));
  }

  public void showEntityIndicator(LivingEntity entity) {
    entitiesWithInvisIndic.remove(entity);

    Optional
        .ofNullable(indicators.get(entity))
        .ifPresent(ind -> ind.setVisibility(VisibilitySettings.Visibility.VISIBLE));
  }

  public boolean attack(LivingEntity victim, Attack attack, AttackInfo attackInfo) {
    if (immunities.containsKey(victim) && immunities.get(victim).containsKey(attackInfo)) {
      return false;
    }

    AttackEvent attackEvent = new AttackEvent(victim, attack, attackInfo);
    Bukkit.getPluginManager().callEvent(attackEvent);

    if (attackEvent.isCancelled()) {
      return false;
    }

    Attribute attribute = attackInfo.getAttribute();
    AttackSource attackSource = new AttackSource(attribute, attack);

    DamageEvent damageEvent = new DamageEvent(victim, attack.getDamage(), false, attackSource);
    Bukkit.getPluginManager().callEvent(damageEvent);

    if (damageEvent.isCancelled()) {
      return false;
    }

    attack.getKb().getFinalKbVector(victim).ifPresent(victim::setVelocity);

    double finalDamage = damageEvent.getFinalDamage();
    victim.setMaximumNoDamageTicks(0);
    victim.setNoDamageTicks(0);
    victim.damage(finalDamage);

    Player damager = attribute.getPlayer();

    if (victim != damager) {
      InGameProfile prof = SSL.getInstance().getGameManager().getProfile(damager);
      prof.getStats().addDamageDealt(finalDamage);
    }

    if (damager != victim) {
      double newCombo = playerComboDamages.getOrDefault(damager, 0.0) + finalDamage;
      damager.setLevel((int) newCombo);
      playerComboDamages.put(damager, newCombo);
      cancelComboRemover(damager);

      comboDamageRemovers.put(damager, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
        clearPlayerCombo(damager);
      }, getComboDuration()));
    }

    clearDamageSource(victim);
    lastDamageSources.put(victim, attackSource);

    lastDamageRemovers.put(victim, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
      clearDamageSource(victim);
    }, SSL.getInstance().getResources().getConfig().getInt("Damage.Lifetime")));

    Optional
        .ofNullable(immunities.putIfAbsent(victim, new HashMap<>()))
        .flatMap(immunities -> Optional.ofNullable(immunities.remove(attackInfo)))
        .ifPresent(BukkitTask::cancel);

    BukkitTask immunityRemover = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
      immunities.get(victim).remove(attackInfo).cancel();
    }, attack.getImmunityTicks());

    immunities.get(victim).put(attackInfo, immunityRemover);

    return true;
  }

  private void cancelComboRemover(Player player) {
    Optional.ofNullable(comboDamageRemovers.remove(player)).ifPresent(BukkitTask::cancel);
  }

  public void clearPlayerCombo(Player player) {
    cancelComboRemover(player);
    playerComboDamages.remove(player);
    player.setLevel(0);
  }

  public void clearDamageSource(LivingEntity entity) {
    lastDamageSources.remove(entity);
    Optional.ofNullable(lastDamageRemovers.remove(entity)).ifPresent(BukkitTask::cancel);
  }

  public void reset() {
    new HashSet<>(indicators.keySet()).forEach(this::clearIndicator);
    new HashSet<>(lastDamageSources.keySet()).forEach(this::clearDamageSource);
    new HashSet<>(playerComboDamages.keySet()).forEach(this::clearPlayerCombo);
    new HashSet<>(immunities.keySet()).forEach(this::clearImmunities);
    entitiesWithInvisIndic.clear();
  }

  public void clearImmunities(LivingEntity entity) {
    Optional.ofNullable(immunities.remove(entity)).ifPresent(immunities -> {
      CollectionUtils.clearOverValues(immunities, BukkitTask::cancel);
    });
  }
}
