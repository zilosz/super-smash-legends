package com.github.zilosz.ssl.damage;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.AttributeDamageEvent;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
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

public class DamageManager {
    private final Map<LivingEntity, DamageIndicator> indicators = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> indicatorRemovers = new HashMap<>();
    private final Set<LivingEntity> entitiesWithInvisibleIndicator = new HashSet<>();

    private final Map<Player, Double> playerComboDamages = new HashMap<>();
    private final Map<Player, BukkitTask> comboDamageRemovers = new HashMap<>();

    private final Map<LivingEntity, Attribute> lastDamagingAttributes = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> damageSourceRemovers = new HashMap<>();

    private final Map<LivingEntity, Set<Attribute>> immunities = new HashMap<>();
    private final Map<LivingEntity, Map<Attribute, BukkitTask>> immunityRemovers = new HashMap<>();

    public Attribute getLastDamagingAttribute(LivingEntity entity) {
        return this.lastDamagingAttributes.get(entity);
    }

    private int getComboDuration() {
        return SSL.getInstance().getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
    }

    public void updateIndicator(LivingEntity entity, double damage) {
        DamageIndicator indicator;

        if (this.indicators.containsKey(entity)) {
            indicator = this.indicators.get(entity);
            this.indicatorRemovers.remove(entity).cancel();
            indicator.updateDamage(damage);

        } else {
            indicator = DamageIndicator.create(SSL.getInstance(), entity, damage);
            this.indicators.put(entity, indicator);

            if (this.entitiesWithInvisibleIndicator.contains(entity)) {
                indicator.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            }
        }

        this.indicatorRemovers.put(entity, Bukkit.getScheduler()
                .runTaskLater(SSL.getInstance(), () -> this.destroyIndicator(entity), this.getComboDuration()));
    }

    public void destroyIndicator(LivingEntity entity) {
        Optional.ofNullable(this.indicators.remove(entity)).ifPresent(DamageIndicator::destroy);
        Optional.ofNullable(this.indicatorRemovers.remove(entity)).ifPresent(BukkitTask::cancel);
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

    public boolean attack(LivingEntity victim, Attribute attribute, Attack attack) {
        if (this.immunities.containsKey(victim) && this.immunities.get(victim).contains(attribute)) return false;

        AttackEvent attackEvent = new AttackEvent(victim, attack, attribute);
        Bukkit.getPluginManager().callEvent(attackEvent);

        if (attackEvent.isCancelled()) return false;

        Damage damage = attack.getDamage();
        AttributeDamageEvent damageEvent = new AttributeDamageEvent(victim, damage, false, attribute);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (!damageEvent.isCancelled()) {
            double finalDamage = damageEvent.getFinalDamage();
            victim.setMaximumNoDamageTicks(0);
            victim.setNoDamageTicks(0);
            victim.damage(finalDamage);

            Player damager = attribute.getPlayer();

            double newCombo = this.playerComboDamages.getOrDefault(damager, 0.0) + finalDamage;
            damager.setLevel((int) newCombo);
            this.playerComboDamages.put(damager, newCombo);

            Optional.ofNullable(this.comboDamageRemovers.remove(damager)).ifPresent(BukkitTask::cancel);

            this.comboDamageRemovers.put(damager, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
                this.clearPlayerCombo(damager);
            }, this.getComboDuration()));

            if (victim != attribute.getPlayer()) {
                InGameProfile damagerProfile = SSL.getInstance().getGameManager().getProfile(attribute.getPlayer());
                damagerProfile.setDamageDealt(damagerProfile.getDamageDealt() + finalDamage);
            }
        }

        AttributeKbEvent kbEvent = new AttributeKbEvent(victim, attack.getKb(), attribute);
        Bukkit.getPluginManager().callEvent(kbEvent);

        if (!kbEvent.isCancelled()) {
            kbEvent.getFinalKbVector().ifPresent(victim::setVelocity);
        }

        this.lastDamagingAttributes.put(victim, attribute);

        this.cancelDamageSourceRemover(victim);
        int damageLifetime = SSL.getInstance().getResources().getConfig().getInt("Damage.Lifetime");

        BukkitTask task = Bukkit.getScheduler()
                .runTaskLater(SSL.getInstance(), () -> this.removeDamageSource(victim), damageLifetime);

        this.damageSourceRemovers.put(victim, task);

        this.immunityRemovers.putIfAbsent(victim, new HashMap<>());
        this.destroyImmunityRemover(victim, attribute);

        this.immunities.putIfAbsent(victim, new HashSet<>());
        this.immunities.get(victim).add(attribute);

        this.immunityRemovers.get(victim).put(attribute, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.destroyImmunityRemover(victim, attribute);
            this.immunities.get(victim).remove(attribute);
        }, attack.getImmunityTicks()));

        return true;
    }

    private void cancelDamageSourceRemover(LivingEntity entity) {
        Optional.ofNullable(this.damageSourceRemovers.get(entity)).ifPresent(BukkitTask::cancel);
    }

    public void removeDamageSource(LivingEntity entity) {
        this.lastDamagingAttributes.remove(entity);
        this.cancelDamageSourceRemover(entity);
    }

    private void destroyImmunityRemover(LivingEntity entity, Attribute attribute) {
        Optional.ofNullable(this.immunityRemovers.get(entity))
                .flatMap(removersByAttribute -> Optional.ofNullable(removersByAttribute.remove(attribute)))
                .ifPresent(BukkitTask::cancel);
    }

    public void clearImmunities(LivingEntity entity) {
        Optional.ofNullable(this.immunities.get(entity)).ifPresent(immunities -> {
            immunities.forEach(attribute -> this.destroyImmunityRemover(entity, attribute));
            immunities.clear();
        });
    }

    private void clearPlayerCombo(Player player) {
        this.playerComboDamages.remove(player);
        player.setLevel(0);
    }

    public void removeComboIndicator(Player player) {
        Optional.ofNullable(this.comboDamageRemovers.remove(player)).ifPresent(BukkitTask::cancel);
        this.clearPlayerCombo(player);
    }

    public void reset() {
        CollectionUtils.removeWhileIteratingOverEntry(this.indicators, DamageIndicator::destroy);
        CollectionUtils.removeWhileIteratingOverEntry(this.indicatorRemovers, BukkitTask::cancel);
        CollectionUtils.removeWhileIteratingOverEntry(this.damageSourceRemovers, BukkitTask::cancel);
        CollectionUtils.removeWhileIteratingOverEntry(this.comboDamageRemovers, BukkitTask::cancel);

        CollectionUtils.removeWhileIteratingOverEntry(
                this.immunityRemovers,
                map -> CollectionUtils.removeWhileIteratingOverEntry(map, BukkitTask::cancel)
        );

        this.entitiesWithInvisibleIndicator.clear();
        this.lastDamagingAttributes.clear();
        this.immunities.clear();
        this.playerComboDamages.clear();
    }
}
