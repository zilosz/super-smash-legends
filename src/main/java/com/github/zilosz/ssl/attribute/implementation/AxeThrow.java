package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.event.player.PlayerInteractEvent;

public class AxeThrow extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new AxeProjectile(this, this.config.getSection("Projectile")).launch();
        this.hotbarItem.hide();
    }

    @Override
    public void onCooldownEnd() {
        this.hotbarItem.show();
    }

    private static class AxeProjectile extends ItemProjectile {

        public AxeProjectile(Ability ability, Section config) {
            super(ability, config);
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
