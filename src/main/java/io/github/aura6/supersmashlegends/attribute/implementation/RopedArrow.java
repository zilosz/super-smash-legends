package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.LeftClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ArrowProjectile;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class RopedArrow extends LeftClickAbility {

    public RopedArrow(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new RopedProjectile(plugin, this, config).launch();
        player.getWorld().playSound(player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 2);
    }

    public static class RopedProjectile extends ArrowProjectile {

        public RopedProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(200, 200, 200).boom(this.plugin, this.entity.getLocation(), 1.2, 0.3, 10);
        }
    }
}
