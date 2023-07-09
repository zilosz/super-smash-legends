package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.LeftClickAbility;
import com.github.zilosz.ssl.projectile.ArrowProjectile;
import com.github.zilosz.ssl.utils.effect.ParticleMaker;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class RopedArrow extends LeftClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new RopedProjectile(this, this.config).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 2);
    }

    private static class RopedProjectile extends ArrowProjectile {

        public RopedProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onTick() {
            new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_NORMAL)).show(this.entity.getLocation());
        }

        @Override
        public void onGeneralHit() {
            Vector direction = VectorUtils.fromTo(this.launcher, this.entity).normalize();
            Vector extra = new Vector(0, this.config.getDouble("ExtraY"), 0);
            this.launcher.setVelocity(direction.multiply(this.config.getDouble("PullStrength")).add(extra));
            this.launcher.playSound(this.launcher.getLocation(), Sound.STEP_WOOD, 1, 1);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(200, 200, 200));
            new ParticleMaker(particle).boom(SSL.getInstance(), this.entity.getLocation(), 1.2, 0.4, 6);
        }
    }
}
