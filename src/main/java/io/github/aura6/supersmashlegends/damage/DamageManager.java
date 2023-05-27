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

    public Optional<Attribute> getLastDamagingAttribute(LivingEntity entity) {
        return Optional.ofNullable(this.lastDamagingAttributes.get(entity.getUniqueId()));
    }

    public void destroyIndicator(LivingEntity entity) {
        Optional.ofNullable(this.indicators.remove(entity.getUniqueId())).ifPresent(DamageIndicator::destroy);
        Optional.ofNullable(this.indicatorRemovers.remove(entity.getUniqueId())).ifPresent(BukkitTask::cancel);
    }

    public void updateIndicator(LivingEntity entity, double damage) {
        UUID uuid = entity.getUniqueId();
        DamageIndicator indicator;

        if (this.indicators.containsKey(uuid)) {
            indicator = this.indicators.get(uuid);
            this.indicatorRemovers.remove(uuid).cancel();

        } else {
            indicator = DamageIndicator.create(this.plugin, entity);
            this.indicators.put(uuid, indicator);
        }

        indicator.stackDamage(damage);

        int comboDuration = this.plugin.getResources().getConfig().getInt("Damage.Indicator.ComboDuration");
        this.indicatorRemovers.put(uuid, Bukkit.getScheduler().runTaskLater(this.plugin, () -> destroyIndicator(entity), comboDuration));
    }

    public void removeDamageSource(LivingEntity entity) {
        this.lastDamagingAttributes.remove(entity.getUniqueId());
        Optional.ofNullable(this.damageSourceRemovers.get(entity.getUniqueId())).ifPresent(BukkitTask::cancel);
    }

    public boolean attemptAttributeDamage(LivingEntity victim, Damage damage, Attribute attribute) {
        UUID victimUuid = victim.getUniqueId();

        if (this.immunities.containsKey(victimUuid) && this.immunities.get(victimUuid).contains(attribute)) return false;

        AttributeDamageEvent event = new AttributeDamageEvent(victim, damage, attribute);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return false;

        this.lastDamagingAttributes.put(victimUuid, attribute);
        int damageLifetime = this.plugin.getResources().getConfig().getInt("Damage.Lifetime");
        this.damageSourceRemovers.put(victimUuid, Bukkit.getScheduler().runTaskLater(this.plugin, () -> removeDamageSource(victim), damageLifetime));

        this.immunities.putIfAbsent(victimUuid, new HashSet<>());
        this.immunities.get(victimUuid).add(attribute);

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
            victim.setVelocity(direction.clone().normalize().multiply(kb).setY(damage.getKbY()));
        });

        updateIndicator(victim, damage.getDamage());

        InGameProfile damagerProfile = this.plugin.getGameManager().getProfile(attribute.getPlayer());
        damagerProfile.setDamageDealt(damagerProfile.getDamageDealt() + damage.getDamage());

        this.immunityRemovers.put(attribute, Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.immunities.get(victimUuid).remove(attribute);
            Optional.ofNullable(this.immunityRemovers.remove(attribute)).ifPresent(BukkitTask::cancel);
        }, damage.getImmunityTicks()));

        return true;
    }

    public void clearImmunities(LivingEntity entity) {
        if (!this.immunities.containsKey(entity.getUniqueId())) return;

        for (Attribute attribute : this.immunities.get(entity.getUniqueId())) {

            if (this.immunityRemovers.containsKey(attribute)) {
                this.immunityRemovers.get(attribute).cancel();
                this.immunityRemovers.remove(attribute);
            }
        }

        this.immunities.get(entity.getUniqueId()).clear();
    }
}
