package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerInteractEvent;

public class GooeyBullet extends RightClickAbility {

    public GooeyBullet(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new GooeyProjectile(plugin, this, config).launch();
    }

    public static class GooeyProjectile extends LivingProjectile<Slime> {

        public GooeyProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public Slime createEntity(Location location) {
            Slime slime = location.getWorld().spawn(location, Slime.class);
            slime.setSize(config.getInt("Size"));
            return slime;
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(124, 252, 0).show(this.entity.getLocation());
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(0, 255, 0).boom(this.plugin, this.entity.getLocation(), 2.5, 0.4, 5);
        }
    }
}
