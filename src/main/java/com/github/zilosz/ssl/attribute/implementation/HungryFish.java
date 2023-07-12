package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attack.KnockBack;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.Noise;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.FloatingEntity;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class HungryFish extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        AttackInfo attackInfo = new AttackInfo(AttackType.HUNGRY_FISH, this);
        new BubbleProjectile(this.config.getSection("Projectile"), attackInfo).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 0.5f, 1);
    }

    private static class BubbleProjectile extends ItemProjectile {
        private BukkitTask soakTask;
        private Listener soakListener;
        private LivingEntity soakedEntity;
        private FloatingEntity<Item> fish;
        private BukkitTask fishMoveTask;

        public BubbleProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.onGeneralHit();
        }

        private void onGeneralHit() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.SPLASH2, 2, 2);
            this.displayBubble(1.5);
        }

        private void displayBubble(double radiusMultiplier) {
            Location center = EntityUtils.center(this.entity);
            double radius = this.hitBox * radiusMultiplier;
            int count = (int) (100 * radiusMultiplier);
            new ParticleMaker(new ParticleBuilder(ParticleEffect.WATER_DROP)).hollowSphere(center, radius, count);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            if (reason != ProjectileRemoveReason.HIT_ENTITY) {
                this.stopSoak();
            }
        }

        @Override
        public void onTick() {
            this.displayBubble(0.05);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.onGeneralHit();

            this.soakedEntity = target;

            double multiplier = this.config.getDouble("VelocityMultiplier");

            if (target.getType() == EntityType.PLAYER) {
                ((Player) target).setWalkSpeed((float) (0.2 * multiplier));
            }

            this.soakTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

                for (int i = 0; i < 5; i++) {
                    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.WATER_DROP);
                    new ParticleMaker(particle).setSpread(0.75, 0.5, 0.75).show(target.getLocation());
                }
            }, 0, 0);

            Noise noise = new Noise(Sound.SPLASH, 0.5f, 1.5f);

            this.soakListener = new Listener() {

                @EventHandler
                public void onPlayerVelocity(PlayerVelocityEvent event) {
                    if (event.getPlayer() == target) {
                        event.setVelocity(event.getVelocity().multiply(multiplier));
                        noise.playForAll(target.getLocation());
                    }
                }

                @EventHandler
                public void onEntityVelocity(AttackEvent event) {
                    if (event.getVictim() == target && !(target instanceof Player)) {
                        KnockBack knockBack = event.getAttack().getKb();
                        knockBack.setKb(knockBack.getKb() * multiplier);
                        knockBack.setKbY(knockBack.getKbY() * multiplier);
                        noise.playForAll(target.getLocation());
                    }
                }

                @EventHandler
                public void onJump(DoubleJumpEvent event) {
                    if (event.getPlayer() == target) {
                        event.setNoise(noise);
                    }
                }
            };

            Bukkit.getPluginManager().registerEvents(this.soakListener, SSL.getInstance());

            this.fish = new FloatingEntity<>() {

                @Override
                public Item createEntity(Location location) {
                    Item fish = location.getWorld().dropItem(location, new ItemStack(Material.RAW_FISH));
                    fish.setPickupDelay(Integer.MAX_VALUE);
                    return fish;
                }
            };

            Location location = this.entity.getLocation();
            this.fish.spawn(location);

            Vector relativeToLoc = VectorUtils.fromTo(target.getLocation(), location);

            this.fishMoveTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
                this.fish.teleport(target.getLocation().add(relativeToLoc));
            }, 0, 0);

            Bukkit.getScheduler().runTaskLater(SSL.getInstance(), this::stopSoak, this.config.getInt("SoakDuration"));
        }

        private void stopSoak() {
            if (this.soakTask == null) return;

            this.soakTask.cancel();
            HandlerList.unregisterAll(this.soakListener);

            this.fish.destroy();
            this.fishMoveTask.cancel();

            if (this.soakedEntity instanceof Player) {
                ((Player) this.soakedEntity).setWalkSpeed(0.2f);
            }
        }
    }
}
