package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.CustomEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.RangeSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Rasenshuriken extends RightClickAbility {
    private BukkitTask task;
    private Location lastLocation;

    public Rasenshuriken(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
        hotbarItem.hide();

        Rasenshuriken instance = this;

        task = new BukkitRunnable() {
            int ticksCharged = 0;

            @Override
            public void run() {

                if (++ticksCharged >= config.getInt("Lifespan")) {
                    reset();
                    return;
                }

                double y = config.getDouble("Height") + config.getDouble("ParticleRadius");
                lastLocation = EntityUtils.top(player).add(0, y, 0);

                if (ticksCharged % 2 == 0) {
                    display(lastLocation, false, config.getSection("Projectile"));
                }

                Bukkit.getPluginManager().callEvent(new RasenshurikenDisplayEvent(instance));

                if (ticksCharged % 7 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.FUSE, 1, 1);
                }
            }

        }.runTaskTimer(plugin, 0, 0);
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getPlayer() != player || task == null) return;

        Shuriken shuriken = new Shuriken(plugin, this, config.getSection("Projectile"));
        lastLocation.setDirection(player.getEyeLocation().getDirection());
        shuriken.setOverrideLocation(lastLocation);
        shuriken.launch();

        reset();
        startCooldown();
    }

    private void reset() {
        if (task == null) return;

        task.cancel();
        task = null;
        hotbarItem.show();

        player.getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 3, 2);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        reset();
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == player && event.getNewSlot() != slot && task != null) {
            reset();
        }
    }

    public static void display(Location loc, boolean tilted, Section config) {
        float pitch = 90;
        float yaw = loc.getYaw();

        if (tilted) {
            float actual = loc.getPitch();
            pitch = actual >= 0 ? actual - 90 : actual + 90;
        }

        for (double radius = 0; radius <= 0.5; radius += 0.1) {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 255).ring(loc, pitch, yaw, radius, 7.5);
        }

        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(173, 216, 230).solidSphere(loc, config.getDouble("ParticleRadius"), 7, 0.1);
    }

    public static class Shuriken extends ItemProjectile {

        public Shuriken(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
            getDamage().setDamage(config.getDouble("MaxDamage"));
            getDamage().setKb(config.getDouble("MaxKb"));
        }

        @Override
        public void onLaunch() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.WITHER_IDLE, 0.5f, 1);
        }

        @Override
        public void onTick() {
            display(this.entity.getLocation(), true, this.config);

            if (this.ticksAlive % 11 == 0) {
                this.entity.getWorld().playSound(this.entity.getLocation(), Sound.WITHER_SHOOT, 0.5f, 1);
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            onHit(target);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            onHit(null);
        }

        private void onHit(LivingEntity avoid) {
            Location loc = this.entity.getLocation();

            entity.getWorld().playSound(loc, Sound.EXPLODE, 1.5f, 1);
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).solidSphere(loc, config.getDouble("Radius"), 40, 0.1);

            double radius = config.getDouble("Radius");
            RangeSelector selector = new DistanceSelector(radius);

            new EntityFinder(plugin, selector).findAll(this.launcher, loc).forEach(target -> {
                if (target == avoid) return;

                double distanceSq = target.getLocation().distanceSquared(loc);
                double damage = YamlReader.decLin(config, "Damage", distanceSq, radius * radius);
                double kb = YamlReader.decLin(config, "Kb", distanceSq, radius * radius);

                Damage dmg = Damage.Builder.fromConfig(config, VectorUtils.fromTo(entity, target)).build();
                dmg.setDamage(damage);
                dmg.setKb(kb);

                plugin.getDamageManager().attemptAttributeDamage(target, dmg, this.ability);
            });
        }
    }

    public static class RasenshurikenDisplayEvent extends CustomEvent {
        @Getter private final Rasenshuriken rasenshuriken;

        protected RasenshurikenDisplayEvent(Rasenshuriken rasenshuriken) {
            this.rasenshuriken = rasenshuriken;
        }
    }
}
