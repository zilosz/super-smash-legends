package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.event.JumpEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.projectile.ProjectileRemoveReason;
import io.github.aura6.supersmashlegends.utils.Noise;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scheduler.BukkitTask;

public class WaterBubble extends RightClickAbility {

    public WaterBubble(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new BubbleProjectile(this.plugin, this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 0.5f, 1);
    }

    private static class BubbleProjectile extends ItemProjectile {
        private LivingEntity soakedEntity;
        private BukkitTask soakTask;

        public BubbleProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        private void displayBubble(double radiusMultiplier) {
            Location center = EntityUtils.center(this.entity);
            double radius = this.config.getDouble("HitBox") * radiusMultiplier;
            int count = (int) (100 * radiusMultiplier);
            new ParticleBuilder(EnumParticle.WATER_DROP).hollowSphere(center, radius, count);
        }

        @Override
        public void onTick() {
            this.displayBubble(0.05);
        }

        private void onGeneralHit() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.SPLASH2, 2, 2);
            this.displayBubble(1.5);
        }

        private void stopSoak() {
            if (this.soakTask != null) {
                this.soakTask.cancel();
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.onGeneralHit();

            this.soakedEntity = target;

            this.soakTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

                for (int i = 0; i < 5; i++) {
                    new ParticleBuilder(EnumParticle.WATER_DROP).setSpread(0.7f, 0.5f, 0.7f).show(target.getLocation());
                }
            }, 0, 0);

            Bukkit.getScheduler().runTaskLater(this.plugin, this::stopSoak, this.config.getInt("SoakDuration"));
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.onGeneralHit();
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            if (reason != ProjectileRemoveReason.HIT_ENTITY) {
                this.stopSoak();
            }
        }

        @EventHandler
        public void onJump(JumpEvent event) {
            if (this.soakedEntity == event.getPlayer()) {
                event.setNoise(new Noise(Sound.SPLASH, 0.5f, 2));
            }
        }

        @EventHandler
        public void onPlayerVelocity(PlayerVelocityEvent event) {
            if (event.getPlayer() == this.soakedEntity) {
                event.setVelocity(event.getVelocity().multiply(this.config.getDouble("VelocityMultiplier")));
            }
        }

        @EventHandler
        public void onEntityVelocity(DamageEvent event) {
            if (event.getVictim() == this.soakedEntity && this.soakedEntity.getType() != EntityType.PLAYER) {
                event.getDamage().setKb(event.getDamage().getKb() * this.config.getDouble("VelocityMultiplier"));
                event.getDamage().setKbY(event.getDamage().getKbY() * this.config.getDouble("VelocityMultiplier"));
            }
        }
    }
}
