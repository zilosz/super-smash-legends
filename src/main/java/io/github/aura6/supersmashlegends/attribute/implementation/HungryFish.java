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
import io.github.aura6.supersmashlegends.utils.entity.FloatingEntity;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
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

    public HungryFish(SuperSmashLegends plugin, Section config, Kit kit) {
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

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.onGeneralHit();
        }

        private void stopSoak() {
            if (this.soakTask == null) return;

            this.soakTask.cancel();
            HandlerList.unregisterAll(this.soakListener);

            this.fish.destroy();
            this.fishMoveTask.cancel();

            if (this.soakedEntity.getType() == EntityType.PLAYER) {
                ((Player) this.soakedEntity).setWalkSpeed(0.2f);
            }
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            if (reason != ProjectileRemoveReason.HIT_ENTITY) {
                this.stopSoak();
            }
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
                public void onEntityVelocity(DamageEvent event) {
                    if (event.getVictim() == target && target.getType() != EntityType.PLAYER) {
                        event.getDamage().setKb(event.getDamage().getKb() * multiplier);
                        event.getDamage().setKbY(event.getDamage().getKbY() * multiplier);
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
    }
}