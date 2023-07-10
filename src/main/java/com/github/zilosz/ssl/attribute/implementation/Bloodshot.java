package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.RunnableUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class Bloodshot extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        RunnableUtils.runTaskWithIntervals(
                SSL.getInstance(),
                this.config.getInt("Count"),
                this.config.getInt("Interval"),
                () -> new BloodProjectile(this, this.config.getSection("Projectile")).launch()
        );
    }

    private static class BloodProjectile extends ItemProjectile {

        public BloodProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onLaunch() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.LAVA_POP, 2, 1);
        }

        @Override
        public void onTick() {
            new ParticleMaker(new ParticleBuilder(ParticleEffect.REDSTONE)).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            int duration = this.config.getInt("PoisonDuration");
            int level = this.config.getInt("PoisonLevel");
            new PotionEffectEvent(target, PotionEffectType.POISON, duration, level).apply();

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
            new ParticleMaker(particle).boom(SSL.getInstance(), this.entity.getLocation(), 3, 0.3, 7);
        }
    }
}
