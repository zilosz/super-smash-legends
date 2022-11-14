package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.LivingProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerInteractEvent;

public class GooeyBullet extends RightClickAbility {

    public GooeyBullet(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new GooeyProjectile(plugin, this, config).launch();
    }

    public static class GooeyProjectile extends LivingProjectile<Slime> {

        public GooeyProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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
