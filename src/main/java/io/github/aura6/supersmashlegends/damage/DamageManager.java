package io.github.aura6.supersmashlegends.damage;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            Section heightConfig = plugin.getResources().getConfig().getSection("Damage.Indicator.Height");
            String heightPath = entity instanceof Player ? "Player" : "Entity";

            indicator = new DamageIndicator(plugin, entity) {

                @Override
                public Location updateLocation() {
                    return EntityUtils.top(entity).add(0, heightConfig.getDouble(heightPath), 0);
                }
            };

            indicator.spawn();
            indicators.put(uuid, indicator);

            if (entity instanceof Player) {
                indicator.hideFrom((Player) entity);
            }
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
        UUID uuid = victim.getUniqueId();

        if (immunities.containsKey(uuid) && immunities.get(uuid).contains(attribute)) return false;

        Damage damage = event.getDamage();

        if (victim instanceof Player) {
            Player player = (Player) victim;
            Kit kit = plugin.getKitManager().getSelectedKit(player);
            damage.setDamage(damage.getDamage() * kit.getArmor());
            damage.setKb(damage.getKb() * kit.getKb());
        }

        if (damage.isFactorsHealth()) {
            double min = plugin.getResources().getConfig().getDouble("Damage.KbHealthMultiplier.Min");
            double max = plugin.getResources().getConfig().getDouble("Damage.KbHealthMultiplier.Max");
            double multiplier = MathUtils.decreasingLinear(min, max, victim.getMaxHealth(), victim.getHealth());
            damage.setKb(damage.getKb() * multiplier);
        }

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        Damage newDamage = event.getDamage();
        victim.damage(newDamage.getDamage());

        Optional.ofNullable(damage.getDirection()).ifPresent(direction -> {
            Vector kb = new Vector(newDamage.getKb(), 1, newDamage.getKb());
            victim.setVelocity(direction.clone().normalize().multiply(kb).setY(newDamage.getKbY()));
        });

        updateIndicator(victim, damage.getDamage());

        lastDamagingAttributes.put(uuid, attribute);
        int damageLifetime = plugin.getResources().getConfig().getInt("Damage.Lifetime");
        damageSourceRemovers.put(uuid, Bukkit.getScheduler().runTaskLater(plugin, () -> removeDamageSource(victim), damageLifetime));

        immunities.putIfAbsent(uuid, new HashSet<>());
        immunities.get(uuid).add(attribute);

        immunityRemovers.put(attribute, Bukkit.getScheduler().runTaskLater(plugin, () -> {
            immunities.get(uuid).remove(attribute);
            immunityRemovers.remove(attribute).cancel();
        }, newDamage.getImmunityTicks()));

        return true;
    }

    public void clearImmunities(LivingEntity entity) {

        if (immunities.containsKey(entity.getUniqueId())) {

            for (Attribute attribute : immunities.get(entity.getUniqueId())) {
                if (immunityRemovers.containsKey(attribute)) {
                    immunityRemovers.get(attribute).cancel();
                    immunityRemovers.remove(attribute);
                }
            }

            immunities.get(entity.getUniqueId()).clear();
        }
    }
}
