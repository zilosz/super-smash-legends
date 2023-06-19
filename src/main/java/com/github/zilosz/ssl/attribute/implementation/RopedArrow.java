package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.LeftClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ArrowProjectile;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class RopedArrow extends LeftClickAbility {

    public RopedArrow(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new RopedProjectile(plugin, this, config).launch();
        player.getWorld().playSound(player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 2);
    }

    public static class RopedProjectile extends ArrowProjectile {

        public RopedProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SMOKE_NORMAL).show(this.entity.getLocation());
        }

        @Override
        public void onGeneralHit() {
            Vector direction = VectorUtils.fromTo(this.launcher, this.entity).normalize();
            Vector extra = new Vector(0, config.getDouble("ExtraY"), 0);
            launcher.setVelocity(direction.multiply(config.getDouble("PullStrength")).add(extra));
            launcher.playSound(launcher.getLocation(), Sound.STEP_WOOD, 1, 1);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            new ParticleBuilder(EnumParticle.REDSTONE)
                    .setRgb(200, 200, 200)
                    .boom(this.plugin, this.entity.getLocation(), 1.2, 0.4, 6);
        }
    }
}
