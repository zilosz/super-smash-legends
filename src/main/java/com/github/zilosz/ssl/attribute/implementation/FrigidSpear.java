package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class FrigidSpear extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        AttackInfo attackInfo = new AttackInfo(AttackType.FRIGID_SPEAR, this);
        new SpearProjectile(this.config.getSection("Projectile"), attackInfo).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.GLASS, 1, 2);
    }

    private static class SpearProjectile extends ItemProjectile {

        public SpearProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onTick() {
            new ParticleMaker(new ParticleBuilder(ParticleEffect.SNOW_SHOVEL)).show(this.entity.getLocation());
        }
    }
}
