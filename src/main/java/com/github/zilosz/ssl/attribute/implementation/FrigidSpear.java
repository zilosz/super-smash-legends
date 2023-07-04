package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

public class FrigidSpear extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpearProjectile(this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.GLASS, 1, 2);
    }

    private static class SpearProjectile extends ItemProjectile {

        public SpearProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SNOW_SHOVEL).show(this.entity.getLocation());
        }
    }
}
