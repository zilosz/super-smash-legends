package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.game.InGameProfile;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AttackManager {
    private final Map<LivingEntity, DamageIndicator> indicators = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> indicatorRemovers = new HashMap<>();
    private final Set<LivingEntity> entitiesWithInvisibleIndicator = new HashSet<>();

    private final Map<Player, Double> playerComboDamages = new HashMap<>();
    private final Map<Player, BukkitTask> comboDamageRemovers = new HashMap<>();

    private final Map<LivingEntity, AttackSource> lastDamageSources = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> lastDamageRemovers = new HashMap<>();

    private final Map<LivingEntity, Map<AttackInfo, BukkitTask>> immunities = new HashMap<>();

    public AttackSource getLastAttackSource(LivingEntity entity) {
        return this.lastDamageSources.get(entity);
    }

    public void updateIndicator(LivingEntity entity, double damage) {
        DamageIndicator indicator;

        if (this.indicators.containsKey(entity)) {
            indicator = this.indicators.get(entity);
            this.indicatorRemovers.remove(entity).cancel();
            indicator.updateDamage(damage);

        } else {
            indicator = DamageIndicator.create(entity, damage);
            this.indicators.put(entity, indicator);

            if (this.entitiesWithInvisibleIndicator.contains(entity)) {
                indicator.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            }
        }

        this.indicatorRemovers.put(entity, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.clearIndicator(entity);
        }, this.getComboDuration()));
    }

    public void clearIndicator(LivingEntity entity) {
        Optional.ofNullable(this.indicators.remove(entity)).ifPresent(DamageIndicator::destroy);
        Optional.ofNullable(this.indicatorRemovers.remove(entity)).ifPresent(BukkitTask::cancel);
    }

    private int getComboDuration() {
        return SSL.getInstance().getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
    }

    public void hideEntityIndicator(LivingEntity entity) {
        this.entitiesWithInvisibleIndicator.add(entity);

        Optional.ofNullable(this.indicators.get(entity))
                .ifPresent(indicator -> indicator.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN));
    }

    public void showEntityIndicator(LivingEntity entity) {
        this.entitiesWithInvisibleIndicator.remove(entity);

        Optional.ofNullable(this.indicators.get(entity))
                .ifPresent(indicator -> indicator.setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE));
    }

    public boolean attack(LivingEntity victim, Attack attack, AttackInfo attackInfo) {
        if (this.immunities.containsKey(victim) && this.immunities.get(victim).containsKey(attackInfo)) return false;

        AttackEvent attackEvent = new AttackEvent(victim, attack, attackInfo);
        Bukkit.getPluginManager().callEvent(attackEvent);

        if (attackEvent.isCancelled()) return false;

        Attribute attribute = attackInfo.getAttribute();
        AttackSource attackSource = new AttackSource(attribute, attack);

        DamageEvent damageEvent = new DamageEvent(victim, attack.getDamage(), false, attackSource);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (damageEvent.isCancelled()) return false;

        attack.getKb().getFinalKbVector(victim).ifPresent(victim::setVelocity);

        double finalDamage = damageEvent.getFinalDamage();
        victim.setMaximumNoDamageTicks(0);
        victim.setNoDamageTicks(0);
        victim.damage(finalDamage);

        if (victim != attribute.getPlayer()) {
            InGameProfile damagerProfile = SSL.getInstance().getGameManager().getProfile(attribute.getPlayer());
            damagerProfile.getStats().setDamageDealt(damagerProfile.getStats().getDamageDealt() + finalDamage);
        }

        Player damager = attribute.getPlayer();

        if (damager != victim) {
            double newCombo = this.playerComboDamages.getOrDefault(damager, 0.0) + finalDamage;
            damager.setLevel((int) newCombo);
            this.playerComboDamages.put(damager, newCombo);
            this.cancelComboRemover(damager);

            this.comboDamageRemovers.put(damager, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
                this.clearPlayerCombo(damager);
            }, this.getComboDuration()));
        }

        this.clearDamageSource(victim);
        this.lastDamageSources.put(victim, attackSource);

        this.lastDamageRemovers.put(victim, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.clearDamageSource(victim);
        }, SSL.getInstance().getResources().getConfig().getInt("Damage.Lifetime")));

        Optional.ofNullable(this.immunities.putIfAbsent(victim, new HashMap<>()))
                .flatMap(immunities -> Optional.ofNullable(immunities.remove(attackInfo)))
                .ifPresent(BukkitTask::cancel);

        this.immunities.get(victim).put(attackInfo, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.immunities.get(victim).remove(attackInfo).cancel();
        }, attack.getImmunityTicks()));

        return true;
    }

    private void cancelComboRemover(Player player) {
        Optional.ofNullable(this.comboDamageRemovers.remove(player)).ifPresent(BukkitTask::cancel);
    }

    public void clearPlayerCombo(Player player) {
        this.cancelComboRemover(player);
        this.playerComboDamages.remove(player);
        player.setLevel(0);
    }

    public void clearDamageSource(LivingEntity entity) {
        this.lastDamageSources.remove(entity);
        Optional.ofNullable(this.lastDamageRemovers.remove(entity)).ifPresent(BukkitTask::cancel);
    }

    public void reset() {
        new HashSet<>(this.indicators.keySet()).forEach(this::clearIndicator);
        new HashSet<>(this.lastDamageSources.keySet()).forEach(this::clearDamageSource);
        new HashSet<>(this.playerComboDamages.keySet()).forEach(this::clearPlayerCombo);
        new HashSet<>(this.immunities.keySet()).forEach(this::clearImmunities);
        this.entitiesWithInvisibleIndicator.clear();
    }

    public void clearImmunities(LivingEntity entity) {
        Optional.ofNullable(this.immunities.remove(entity)).ifPresent(immunities -> {
            CollectionUtils.removeWhileIteratingOverValues(immunities, BukkitTask::cancel);
        });
    }
}
