package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlindingFists extends PassiveAbility {
    private final Map<LivingEntity, Integer> chainCounts = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> chainResetters = new HashMap<>();

    public BlindingFists(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Melee";
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.chainCounts.clear();
        this.chainResetters.values().forEach(BukkitTask::cancel);
        this.chainResetters.clear();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() == this.player && event.getDamager() instanceof LivingEntity) {
            this.chainCounts.remove((LivingEntity) event.getDamager());
        }
    }

    @EventHandler
    public void onCustomDamage(AttributeDamageEvent event) {
        LivingEntity victim = event.getVictim();
        Player damager = event.getAttribute().getPlayer();

        if (victim == this.player) {
            this.chainCounts.remove(damager);
            return;
        }

        if (damager != this.player || !(event.getAttribute() instanceof Melee)) return;

        int maxChain = this.config.getInt("MaxChain");

        this.chainCounts.putIfAbsent(victim, 0);
        int currChain = this.chainCounts.get(victim);

        if (currChain > 0) {
            double pitch = MathUtils.increasingLinear(0.5, 2, maxChain - 1, currChain - 1);
            this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_REMEDY, 0.5f, (float) pitch);

            Location center = EntityUtils.center(event.getVictim());
            new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, center, 1.5, 0.375, 8);

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
        }

        double maxDamageMul = this.config.getDouble("MaxDamageMultiplier");
        double damageMul = MathUtils.increasingLinear(1, maxDamageMul, maxChain - 1, currChain);

        double maxKbMul = this.config.getDouble("MaxKbMultiplier");
        double kbMul = MathUtils.increasingLinear(1, maxKbMul, maxChain - 1, currChain);

        double maxKbYMul = this.config.getDouble("MaxKbYMultiplier");
        double kbYMul = MathUtils.increasingLinear(1, maxKbYMul, maxChain - 1, currChain);

        Damage damage = event.getDamage();
        damage.setDamage(damage.getDamage() * damageMul);
        damage.setKb(damage.getKb() * kbMul);
        damage.setKbY(damage.getKbY() * kbYMul);

        Optional.ofNullable(this.chainResetters.remove(victim)).ifPresent(BukkitTask::cancel);

        this.chainResetters.put(victim, Bukkit.getScheduler()
                .runTaskLater(this.plugin, () -> this.chainCounts.remove(victim), this.config.getInt("ChainDuration")));

        if (currChain < maxChain - 1) {
            this.chainCounts.put(victim, this.chainCounts.get(victim) + 1);
        }
    }
}
