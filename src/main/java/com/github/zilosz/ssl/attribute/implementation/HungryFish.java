package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
import com.github.zilosz.ssl.event.attribute.JumpEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.Noise;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.FloatingEntity;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
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

public class HungryFish extends RightClickAbility {

    public HungryFish(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new BubbleProjectile(this.plugin, this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 0.5f, 1);
    }

    private static class BubbleProjectile extends ItemProjectile {
        private BukkitTask soakTask;
        private Listener soakListener;
        private LivingEntity soakedEntity;
        private FloatingEntity<Item> fish;
        private BukkitTask fishMoveTask;

        public BubbleProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
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
            new ParticleBuilder(EnumParticle.WATER_DROP).hollowSphere(center, radius, count);
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

            this.soakTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

                for (int i = 0; i < 5; i++) {
                    new ParticleBuilder(EnumParticle.WATER_DROP).setSpread(0.7f, 0.5f, 0.7f).show(target.getLocation());
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
                public void onEntityVelocity(AttributeKbEvent event) {
                    if (event.getVictim() == target && !(target instanceof Player)) {
                        event.getKbSettings().setKb(event.getKbSettings().getKb() * multiplier);
                        event.getKbSettings().setKbY(event.getKbSettings().getKbY() * multiplier);
                        noise.playForAll(target.getLocation());
                    }
                }

                @EventHandler
                public void onJump(JumpEvent event) {
                    if (event.getPlayer() == target) {
                        event.setNoise(noise);
                    }
                }
            };

            Bukkit.getPluginManager().registerEvents(this.soakListener, this.plugin);

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

            this.fishMoveTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                this.fish.teleport(target.getLocation().add(relativeToLoc));
            }, 0, 0);

            Bukkit.getScheduler().runTaskLater(this.plugin, this::stopSoak, this.config.getInt("SoakDuration"));
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
