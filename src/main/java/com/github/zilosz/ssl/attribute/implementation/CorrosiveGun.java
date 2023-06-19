package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ActualProjectile;
import com.github.zilosz.ssl.utils.RunnableUtils;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CorrosiveGun extends RightClickAbility {

    public CorrosiveGun(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        RunnableUtils.runTaskWithIntervals(plugin, config.getInt("Count"), config.getInt("Interval"), () -> {
            new SkullProjectile(plugin, this, config).launch();
            player.setVelocity(player.getEyeLocation().getDirection().multiply(-config.getDouble("Recoil")));
        });
    }

    public static class SkullProjectile extends ActualProjectile<WitherSkull> {

        public SkullProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public WitherSkull createProjectile(Location location) {
            WitherSkull skull = this.launcher.launchProjectile(WitherSkull.class);
            skull.setYield(0);
            skull.setIsIncendiary(false);
            return skull;
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).boom(this.plugin, this.entity.getLocation(), 3.5, 0.5, 6);
        }

        @Override
        public void onTick() {
            for (int i = 0; i < 2; i++) {
                new ParticleBuilder(EnumParticle.FLAME).show(this.entity.getLocation());
            }
        }

        @EventHandler
        public void onEntityExplode(EntityExplodeEvent event) {
            if (event.getEntity() == entity) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onExplosionPrime(ExplosionPrimeEvent event) {
            if (event.getEntity() == entity) {
                event.setCancelled(true);
            }
        }
    }
}
