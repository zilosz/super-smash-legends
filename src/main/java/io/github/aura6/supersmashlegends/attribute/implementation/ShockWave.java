package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.BlockProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;

public class ShockWave extends RightClickAbility {

    public ShockWave(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        float yawOffset = -config.getFloat("YawWidth") / 2;
        float step = config.getFloat("YawWidth") / config.getInt("Count");

        Location launch = player.getEyeLocation();
        float ogYaw = launch.getYaw();

        for (int i = 0; i < config.getInt("Count"); i++) {
            launch.setYaw(ogYaw + yawOffset);

            ShockProjectile projectile = new ShockProjectile(plugin, this, config.getSection("Projectile"));
            projectile.setOverrideLocation(launch);
            projectile.launch();

            yawOffset += step;
        }
    }

    public static class ShockProjectile extends BlockProjectile {

        public ShockProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).setFace(result.getFace())
                    .boom(this.plugin, this.entity.getLocation(), 1.2, 0.4, 1);
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).boom(this.plugin, victim.getLocation(), 5, 0.5, 11);
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.AMBIENCE_THUNDER, 2, 1);
        }

        @Override
        public void onTick() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.FUSE, 1, 1);
        }
    }
}