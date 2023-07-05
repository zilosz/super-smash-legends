package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerInteractEvent;

public class GooeyBullet extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new GooeyProjectile(this, this.config).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SLIME_WALK2, 2, 2);
    }

    private static class GooeyProjectile extends LivingProjectile<Slime> {

        public GooeyProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public Slime createEntity(Location location) {
            Slime slime = location.getWorld().spawn(location, Slime.class);
            slime.setSize(this.config.getInt("Size"));
            return slime;
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.SLIME_WALK, 2, 1);

            new ParticleBuilder(EnumParticle.REDSTONE)
                    .setRgb(0, 255, 0)
                    .boom(SSL.getInstance(), this.entity.getLocation(), 2.5, 0.4, 5);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(124, 252, 0).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.SLIME_ATTACK, 2, 1);
        }
    }
}
