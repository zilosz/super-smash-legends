package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.RunnableUtils;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

public class Bloodshot extends RightClickAbility {

    public Bloodshot(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        RunnableUtils.runTaskWithIntervals(
                this.plugin,
                this.config.getInt("Count"),
                this.config.getInt("Interval"),
                () -> new BloodProjectile(this.plugin, this, this.config.getSection("Projectile")).launch()
        );
    }

    public static class BloodProjectile extends ItemProjectile {

        public BloodProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            int duration = this.config.getInt("PoisonDuration");
            int level = this.config.getInt("PoisonLevel");
            new PotionEffectEvent(target, PotionEffectType.POISON, duration, level).apply();

            new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, this.entity.getLocation(), 3, 0.3, 7);
        }
    }
}
