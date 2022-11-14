package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.RunnableUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Bloodshot extends RightClickAbility {

    public Bloodshot(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        RunnableUtils.runTaskWithIntervals(plugin, config.getInt("Count"), config.getInt("Interval"), () -> {
            new BloodProjectile(plugin, this, config.getSection("Projectile")).launch();
            player.getWorld().playSound(player.getEyeLocation(), Sound.LAVA_POP, 1, 2);
        });
    }

    public static class BloodProjectile extends ItemProjectile {

        public BloodProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, config.getInt("PoisonDuration"), config.getInt("PoisonLevel")));
            new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, this.entity.getLocation(), 3, 0.3, 7);
        }
    }
}
