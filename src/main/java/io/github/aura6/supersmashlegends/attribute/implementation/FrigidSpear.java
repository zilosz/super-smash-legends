package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.event.player.PlayerInteractEvent;

public class FrigidSpear extends RightClickAbility {

    public FrigidSpear(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpearProjectile(plugin, this, config.getSection("Projectile")).launch();
    }

    public static class SpearProjectile extends ItemProjectile {

        public SpearProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SNOW_SHOVEL).show(this.entity.getLocation());
        }
    }
}
