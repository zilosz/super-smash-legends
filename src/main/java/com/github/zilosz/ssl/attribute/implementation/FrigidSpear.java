package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.event.player.PlayerInteractEvent;

public class FrigidSpear extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpearProjectile(SSL.getInstance(), this, this.config.getSection("Projectile")).launch();
    }

    public static class SpearProjectile extends ItemProjectile {

        public SpearProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SNOW_SHOVEL).show(this.entity.getLocation());
        }
    }
}
