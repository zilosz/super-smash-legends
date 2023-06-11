package io.github.aura6.supersmashlegends.damage;

import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.game.InGameProfile;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DamageManager {
    private final SuperSmashLegends plugin;

    private final Map<LivingEntity, DamageIndicator> indicators = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> indicatorRemovers = new HashMap<>();

    private final Map<LivingEntity, Attribute> lastDamagingAttributes = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> damageSourceRemovers = new HashMap<>();

    private final Map<LivingEntity, Set<Attribute>> immunities = new HashMap<>();
    private final Map<LivingEntity, Map<Attribute, BukkitTask>> immunityRemovers = new HashMap<>();

    public DamageManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    public Optional<Attribute> getLastDamagingAttribute(LivingEntity entity) {
        return Optional.ofNullable(this.lastDamagingAttributes.get(entity));
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
        }

        indicator.stackDamage(damage);

        int comboDuration = this.plugin.getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
        this.indicatorRemovers.put(entity, Bukkit.getScheduler().runTaskLater(this.plugin, () -> destroyIndicator(entity), comboDuration));
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

    public boolean attemptAttributeDamage(LivingEntity victim, Damage damage, Attribute attribute) {
        if (this.immunities.containsKey(victim) && this.immunities.get(victim).contains(attribute)) return false;

        AttributeDamageEvent event = new AttributeDamageEvent(victim, damage, attribute);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        this.lastDamagingAttributes.put(victim, attribute);

        this.cancelDamageSourceRemover(victim);
        int damageLifetime = this.plugin.getResources().getConfig().getInt("Damage.Lifetime");

        this.damageSourceRemovers.put(victim, Bukkit.getScheduler()
                .runTaskLater(this.plugin, () -> removeDamageSource(victim), damageLifetime));

        if (victim instanceof Player) {
            Player player = (Player) victim;
            Kit kit = this.plugin.getKitManager().getSelectedKit(player);

            if (damage.isFactorsArmor()) {
                damage.setDamage(damage.getDamage() * kit.getArmor());
            }

            if (damage.isFactorsKb()) {
                damage.setKb(damage.getKb() * kit.getKb());
            }
        }

        if (damage.isFactorsHealth()) {
            double min = this.plugin.getResources().getConfig().getDouble("Damage.KbHealthMultiplier.Min");
            double max = this.plugin.getResources().getConfig().getDouble("Damage.KbHealthMultiplier.Max");
            double multiplier = MathUtils.decreasingLinear(min, max, victim.getMaxHealth(), victim.getHealth());
            damage.setKb(damage.getKb() * multiplier);
        }

        victim.damage(damage.getDamage());
        attribute.getPlayer().setLevel((int) damage.getDamage());

        Optional.ofNullable(damage.getDirection()).ifPresent(direction -> {
            Vector kb = new Vector(damage.getKb(), 1, damage.getKb());
            Vector velocity = direction.clone().setY(0).normalize().multiply(kb);
            velocity.setY(damage.isLinearKb() ? direction.getY() : damage.getKbY());

            if (damage.isKbFactorsPreviousVelocity()) {
                velocity = victim.getVelocity().add(velocity);
            }

            victim.setVelocity(velocity);
        });

        this.updateIndicator(victim, damage.getDamage());

        InGameProfile damagerProfile = this.plugin.getGameManager().getProfile(attribute.getPlayer());
        damagerProfile.setDamageDealt(damagerProfile.getDamageDealt() + damage.getDamage());

        this.immunityRemovers.putIfAbsent(victim, new HashMap<>());
        this.destroyImmunityRemover(victim, attribute);

        this.immunities.putIfAbsent(victim, new HashSet<>());
        this.immunities.get(victim).add(attribute);

        this.immunityRemovers.get(victim).put(attribute, Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.destroyImmunityRemover(victim, attribute);
            this.immunities.get(victim).remove(attribute);
        }, damage.getImmunityTicks()));

        return true;
    }

    public void clearImmunities(LivingEntity entity) {
        if (this.immunities.containsKey(entity)) {
            this.immunities.get(entity).forEach(attribute -> this.destroyImmunityRemover(entity, attribute));
            this.immunities.get(entity).clear();
        }
    }
}
