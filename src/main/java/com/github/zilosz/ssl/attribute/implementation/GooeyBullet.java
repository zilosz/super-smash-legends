package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class GooeyBullet extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new GooeyProjectile(this.config, new AttackInfo(AttackType.GOOEY_BULLET, this)).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SLIME_WALK2, 2, 2);
    }

    private static class GooeyProjectile extends LivingProjectile<Slime> {

        public GooeyProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
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

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(0, 255, 0));
            new ParticleMaker(particle).boom(SSL.getInstance(), this.entity.getLocation(), 2.5, 0.4, 5);
        }

        @Override
        public void onTick() {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(124, 252, 0));
            new ParticleMaker(particle).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.SLIME_ATTACK, 2, 1);
        }
    }
}
