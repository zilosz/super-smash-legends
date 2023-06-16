package io.github.aura6.supersmashlegends.damage;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.attack.AttackEvent;
import io.github.aura6.supersmashlegends.event.attack.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.event.attack.AttributeKbEvent;
import io.github.aura6.supersmashlegends.game.InGameProfile;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DamageManager {
    private final SuperSmashLegends plugin;

    private final Map<LivingEntity, DamageIndicator> indicators = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> indicatorRemovers = new HashMap<>();
    private final Set<LivingEntity> entitiesWithInvisibleIndicator = new HashSet<>();

    private final Map<LivingEntity, Attribute> lastDamagingAttributes = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> damageSourceRemovers = new HashMap<>();

    private final Map<LivingEntity, Set<Attribute>> immunities = new HashMap<>();
    private final Map<LivingEntity, Map<Attribute, BukkitTask>> immunityRemovers = new HashMap<>();

    public DamageManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    public Attribute getLastDamagingAttribute(LivingEntity entity) {
        return this.lastDamagingAttributes.get(entity);
    }

    public void destroyIndicator(LivingEntity entity) {
        Optional.ofNullable(this.indicators.remove(entity)).ifPresent(DamageIndicator::destroy);
        Optional.ofNullable(this.indicatorRemovers.remove(entity)).ifPresent(BukkitTask::cancel);
    }

    public void updateIndicator(LivingEntity entity, double damage) {
        DamageIndicator indicator;

        if (this.indicators.containsKey(entity)) {
            indicator = this.indicators.get(entity);
            this.indicatorRemovers.remove(entity).cancel();

        } else {
            indicator = DamageIndicator.create(this.plugin, entity);
            this.indicators.put(entity, indicator);

            if (this.entitiesWithInvisibleIndicator.contains(entity)) {
                indicator.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            }
        }

        indicator.stackDamage(damage);

        int comboDuration = this.plugin.getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
        this.indicatorRemovers.put(entity, Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.destroyIndicator(entity), comboDuration));
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

    private void cancelDamageSourceRemover(LivingEntity entity) {
        Optional.ofNullable(this.damageSourceRemovers.get(entity)).ifPresent(BukkitTask::cancel);
    }

    private void destroyImmunityRemover(LivingEntity entity, Attribute attribute) {
        Optional.ofNullable(this.immunityRemovers.get(entity))
                .flatMap(removersByAttribute -> Optional.ofNullable(removersByAttribute.remove(attribute)))
                .ifPresent(BukkitTask::cancel);
    }

    public void removeDamageSource(LivingEntity entity) {
        this.lastDamagingAttributes.remove(entity);
        this.cancelDamageSourceRemover(entity);
    }

    public boolean attack(LivingEntity victim, Attribute attribute, AttackSettings attackSettings) {
        if (this.immunities.containsKey(victim) && this.immunities.get(victim).contains(attribute)) return false;

        DamageSettings settings = attackSettings.getDamageSettings();
        AttributeDamageEvent damageEvent = new AttributeDamageEvent(victim, settings, false, attribute);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (damageEvent.isCancelled()) return false;

        AttributeKbEvent kbEvent = new AttributeKbEvent(victim, attackSettings.getKbSettings(), attribute);
        Bukkit.getPluginManager().callEvent(kbEvent);

        DamageSettings damageSettings = damageEvent.getDamageSettings();
        KbSettings kbSettings = kbEvent.getKbSettings();
        int immunityTicks = attackSettings.getImmunityTicks();

        AttackSettings newAttackSettings = new AttackSettings(damageSettings, kbSettings, immunityTicks);
        AttackEvent attackEvent = new AttackEvent(victim, newAttackSettings, attribute);
        Bukkit.getPluginManager().callEvent(attackEvent);

        if (attackEvent.isCancelled()) {
            kbEvent.setCancelled(true);
            return false;
        }

        double finalDamage = attackEvent.getFinalDamage();
        victim.setMaximumNoDamageTicks(0);
        victim.setNoDamageTicks(0);
        victim.damage(finalDamage);
        attribute.getPlayer().setLevel((int) finalDamage);

        InGameProfile damagerProfile = this.plugin.getGameManager().getProfile(attribute.getPlayer());
        damagerProfile.setDamageDealt(damagerProfile.getDamageDealt() + finalDamage);

        if (!kbEvent.isCancelled()) {
            attackEvent.getFinalKbVector().ifPresent(victim::setVelocity);
        }

        this.lastDamagingAttributes.put(victim, attribute);

        this.cancelDamageSourceRemover(victim);
        int damageLifetime = this.plugin.getResources().getConfig().getInt("Damage.Lifetime");

        this.damageSourceRemovers.put(victim, Bukkit.getScheduler()
                .runTaskLater(this.plugin, () -> this.removeDamageSource(victim), damageLifetime));

        this.immunityRemovers.putIfAbsent(victim, new HashMap<>());
        this.destroyImmunityRemover(victim, attribute);

        this.immunities.putIfAbsent(victim, new HashSet<>());
        this.immunities.get(victim).add(attribute);

        this.immunityRemovers.get(victim).put(attribute, Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.destroyImmunityRemover(victim, attribute);
            this.immunities.get(victim).remove(attribute);
        }, attackSettings.getImmunityTicks()));

        return true;
    }

    public void clearImmunities(LivingEntity entity) {
        Optional.ofNullable(this.immunities.get(entity)).ifPresent(immunities -> {
            immunities.forEach(attribute -> this.destroyImmunityRemover(entity, attribute));
            immunities.clear();
        });
    }

    public void reset() {
        this.indicators.values().forEach(DamageIndicator::destroy);
        this.indicators.clear();

        this.indicatorRemovers.values().forEach(BukkitTask::cancel);
        this.indicators.clear();

        this.entitiesWithInvisibleIndicator.clear();
        this.lastDamagingAttributes.clear();

        this.damageSourceRemovers.values().forEach(BukkitTask::cancel);
        this.damageSourceRemovers.clear();

        this.immunities.clear();

        this.immunityRemovers.values().forEach(map -> map.values().forEach(BukkitTask::cancel));
        this.immunityRemovers.clear();
    }
}
