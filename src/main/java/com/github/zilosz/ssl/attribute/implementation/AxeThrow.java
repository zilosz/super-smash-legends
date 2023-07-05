package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;

public class AxeThrow extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new AxeProjectile(this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_THROW, 2, 1);
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
            this.ability.getHotbarItem().show();
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
        }
    }
}
