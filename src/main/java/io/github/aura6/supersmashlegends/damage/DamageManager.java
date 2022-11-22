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
import java.util.UUID;

public class DamageManager {
    private final SuperSmashLegends plugin;

    private final Map<UUID, DamageIndicator> indicators = new HashMap<>();
    private final Map<UUID, BukkitTask> indicatorRemovers = new HashMap<>();

    private final Map<UUID, Attribute> lastDamagingAttributes = new HashMap<>();
    private final Map<UUID, BukkitTask> damageSourceRemovers = new HashMap<>();

    private final Map<UUID, Set<Attribute>> immunities = new HashMap<>();
    private final Map<Attribute, BukkitTask> immunityRemovers = new HashMap<>();

    public DamageManager(SuperSmashLegends plugin) {
        this.plugin = plugin;
    }

    public Attribute getLastDamagingAttribute(LivingEntity entity) {
        return lastDamagingAttributes.getOrDefault(entity.getUniqueId(), null);
    }

    public void destroyIndicator(LivingEntity entity) {
        Optional.ofNullable(indicators.remove(entity.getUniqueId())).ifPresent(DamageIndicator::destroy);
        Optional.ofNullable(indicatorRemovers.remove(entity.getUniqueId())).ifPresent(BukkitTask::cancel);
    }

    public void updateIndicator(LivingEntity entity, double damage) {
        UUID uuid = entity.getUniqueId();
        DamageIndicator indicator;

        if (indicators.containsKey(uuid)) {
            indicator = indicators.get(uuid);
            indicatorRemovers.remove(uuid).cancel();

        } else {
            indicator = DamageIndicator.create(plugin, entity);
            indicators.put(uuid, indicator);
        }

        indicator.stackDamage(damage);

        int comboDuration = plugin.getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
        indicatorRemovers.put(uuid, Bukkit.getScheduler().runTaskLater(plugin, () -> destroyIndicator(entity), comboDuration));
    }

    public void removeDamageSource(LivingEntity entity) {
        lastDamagingAttributes.remove(entity.getUniqueId());
        Optional.ofNullable(damageSourceRemovers.get(entity.getUniqueId())).ifPresent(BukkitTask::cancel);
    }

    public boolean attemptAttributeDamage(AttributeDamageEvent event) {
        Attribute attribute = event.getAttribute();

        LivingEntity victim = event.getVictim();
        UUID victimUuid = victim.getUniqueId();

        if (immunities.containsKey(victimUuid) && immunities.get(victimUuid).contains(attribute)) return false;

        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        lastDamagingAttributes.put(victimUuid, attribute);
        int damageLifetime = plugin.getResources().getConfig().getInt("Damage.Lifetime");
        damageSourceRemovers.put(victimUuid, Bukkit.getScheduler().runTaskLater(plugin, () -> removeDamageSource(victim), damageLifetime));

        immunities.putIfAbsent(victimUuid, new HashSet<>());
        immunities.get(victimUuid).add(attribute);

        Damage newDamage = event.getDamage();

        if (victim instanceof Player) {
            Player player = (Player) victim;
            Kit kit = plugin.getKitManager().getSelectedKit(player);

            if (newDamage.isFactorsArmor()) {
                newDamage.setDamage(newDamage.getDamage() * kit.getArmor());
            }

            if (newDamage.isFactorsKb()) {
                newDamage.setKb(newDamage.getKb() * kit.getKb());
            }
        }

        if (newDamage.isFactorsHealth()) {
            double min = plugin.getResources().getConfig().getDouble("Damage.KbHealthMultiplier.Min");
            double max = plugin.getResources().getConfig().getDouble("Damage.KbHealthMultiplier.Max");
            double multiplier = MathUtils.decreasingLinear(min, max, victim.getMaxHealth(), victim.getHealth());
            newDamage.setKb(newDamage.getKb() * multiplier);
        }

        victim.damage(newDamage.getDamage());
        attribute.getPlayer().setLevel((int) newDamage.getDamage());

        Optional.ofNullable(newDamage.getDirection()).ifPresent(direction -> {
            Vector kb = new Vector(newDamage.getKb(), 1, newDamage.getKb());
            victim.setVelocity(direction.clone().normalize().multiply(kb).setY(newDamage.getKbY()));
        });

        updateIndicator(victim, newDamage.getDamage());

        InGameProfile damagerProfile = plugin.getGameManager().getProfile(attribute.getPlayer());
        damagerProfile.setDamageDealt(damagerProfile.getDamageDealt() + newDamage.getDamage());

        immunityRemovers.put(attribute, Bukkit.getScheduler().runTaskLater(plugin, () -> {
            immunities.get(victimUuid).remove(attribute);
            immunityRemovers.remove(attribute).cancel();
        }, newDamage.getImmunityTicks()));

        return true;
    }

    public void clearImmunities(LivingEntity entity) {
        if (!immunities.containsKey(entity.getUniqueId())) return;

        for (Attribute attribute : immunities.get(entity.getUniqueId())) {

            if (immunityRemovers.containsKey(attribute)) {
                immunityRemovers.get(attribute).cancel();
                immunityRemovers.remove(attribute);
            }
        }

        immunities.get(entity.getUniqueId()).clear();
    }
}
