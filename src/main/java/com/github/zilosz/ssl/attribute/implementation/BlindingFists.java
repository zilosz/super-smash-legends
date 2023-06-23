package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.PassiveAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.damage.KbSettings;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.math.MathUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlindingFists extends PassiveAbility {
    private final Map<LivingEntity, Integer> chainCounts = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> chainResetters = new HashMap<>();

    @Override
    public void deactivate() {
        super.deactivate();
        this.chainCounts.clear();
        this.player.removePotionEffect(PotionEffectType.SPEED);
        this.chainResetters.values().forEach(BukkitTask::cancel);
        this.chainResetters.clear();
    }

    @Override
    public String getUseType() {
        return "Melee";
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() == this.player && event.getDamager() instanceof LivingEntity) {
            this.resetChains();
        }
    }

    private void resetChains() {
        this.chainCounts.clear();
        this.player.removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCustomDamage(AttackEvent event) {
        LivingEntity victim = event.getVictim();

        if (victim == this.player) {
            this.resetChains();
            return;
        }

        Attribute attribute = event.getAttribute();

        if (attribute.getPlayer() != this.player) return;
        if (!(attribute instanceof Melee) && !(attribute instanceof SuperhumanPunch)) return;

        int maxChain = this.config.getInt("MaxChain");

        this.chainCounts.putIfAbsent(victim, 0);
        int currChain = this.chainCounts.get(victim);

        if (currChain > 0) {
            double pitch = MathUtils.increasingLinear(0.5, 2, maxChain - 1, currChain - 1);
            this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_REMEDY, 0.5f, (float) pitch);

            Location center = EntityUtils.center(victim);
            new ParticleBuilder(EnumParticle.REDSTONE).boom(SSL.getInstance(), center, 1.5, 0.375, 5);

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
                new PotionEffectEvent(this.player, PotionEffectType.SPEED, 10_000, 1).apply();
            }
        }

        double maxDamageMul = this.config.getDouble("MaxDamageMultiplier");
        double damageMul = MathUtils.increasingLinear(1, maxDamageMul, maxChain - 1, currChain);

        double maxKbMul = this.config.getDouble("MaxKbMultiplier");
        double kbMul = MathUtils.increasingLinear(1, maxKbMul, maxChain - 1, currChain);

        double maxKbYMul = this.config.getDouble("MaxKbYMultiplier");
        double kbYMul = MathUtils.increasingLinear(1, maxKbYMul, maxChain - 1, currChain);

        AttackSettings settings = event.getAttackSettings();
        settings.getDamageSettings().setDamage(settings.getDamageSettings().getDamage() * damageMul);

        KbSettings kbSettings = settings.getKbSettings();
        kbSettings.setKb(kbSettings.getKb() * kbMul);
        kbSettings.setKbY(kbSettings.getKbY() * kbYMul);

        Optional.ofNullable(this.chainResetters.remove(victim)).ifPresent(BukkitTask::cancel);

        this.chainResetters.put(victim, Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.chainCounts.remove(victim);
            this.player.removePotionEffect(PotionEffectType.SPEED);
        }, this.config.getInt("ChainDuration")));

        if (currChain < maxChain - 1) {
            this.chainCounts.put(victim, this.chainCounts.get(victim) + 1);
        }
    }
}
