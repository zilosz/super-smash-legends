package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.projectile.ProjectileRemoveReason;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.event.player.PlayerInteractEvent;

public class AxeThrow extends RightClickAbility {

    public AxeThrow(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new AxeProjectile(plugin, this, config.getSection("Projectile")).launch();
        hotbarItem.hide();
    }

    @Override
    public void onCooldownEnd() {
        hotbarItem.show();
    }

    public static class AxeProjectile extends ItemProjectile {

        public AxeProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            if (this.ability.isEnabled()) {
                this.ability.getHotbarItem().show();
            }
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).show(this.entity.getLocation());
        }
    }
}
