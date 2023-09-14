package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class AxeThrow extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new AxeProjectile(this.config.getSection("Projectile"), new AttackInfo(AttackType.AXE_THROW, this)).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_THROW, 2, 1);
        this.hotbarItem.hide();
    }

    @Override
    public void onCooldownEnd() {
        this.hotbarItem.show();
    }

    private static class AxeProjectile extends ItemProjectile {

        public AxeProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            ((AxeThrow) this.attackInfo.getAttribute()).getHotbarItem().show();
        }

        @Override
        public void onTick() {
            new ParticleMaker(new ParticleBuilder(ParticleEffect.REDSTONE)).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
        }
    }
}
