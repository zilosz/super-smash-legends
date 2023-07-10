package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.CustomEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Rasenshuriken extends RightClickAbility {
    private BukkitTask task;
    private Location lastLocation;
    private int ticksCharged = -1;

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
        this.hotbarItem.hide();

        this.task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (++this.ticksCharged >= this.config.getInt("Lifespan")) {
                this.reset(true);
                return;
            }

            double y = this.config.getDouble("Height") + this.config.getDouble("ParticleRadius");
            this.lastLocation = EntityUtils.top(this.player).add(0, y, 0);

            if (this.ticksCharged % 2 == 0) {
                display(this.lastLocation, false, this.config.getSection("Projectile"));
            }

            Bukkit.getPluginManager().callEvent(new RasenshurikenDisplayEvent(this));

            if (this.ticksCharged % 7 == 0) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.FUSE, 1, 1);
            }
        }, 0, 0);
    }

    private void reset(boolean cooldown) {
        if (this.ticksCharged == -1) return;

        this.ticksCharged = -1;
        this.task.cancel();
        this.hotbarItem.show();

        if (cooldown) {
            this.startCooldown();
        }

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIRE_IGNITE, 3, 2);
    }

    public static void display(Location loc, boolean tilted, Section config) {
        float pitch = 90;
        float yaw = loc.getYaw();

        if (tilted) {
            float actual = loc.getPitch();
            pitch = actual >= 0 ? actual - 90 : actual + 90;
        }

        for (double radius = 0; radius <= 0.5; radius += 0.16) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(255, 255, 255));
            new ParticleMaker(particle).ring(loc, pitch, yaw, radius, 18);
        }

        double radius = config.getDouble("ParticleRadius");
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(173, 216, 230));
        new ParticleMaker(particle).hollowSphere(loc, radius, 5);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getPlayer() != this.player || this.ticksCharged == -1) return;

        Shuriken shuriken = new Shuriken(this, this.config.getSection("Projectile"));
        this.lastLocation.setDirection(this.player.getEyeLocation().getDirection());
        shuriken.setOverrideLocation(this.lastLocation);
        shuriken.launch();

        this.reset(true);
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == this.player && event.getNewSlot() != this.slot && this.ticksCharged > -1) {
            this.reset(true);
        }
    }

    public static class Shuriken extends ItemProjectile {

        public Shuriken(Ability ability, Section config) {
            super(ability, config);
            this.getAttack().getDamage().setDamage(config.getDouble("MaxDamage"));
            this.getAttack().getKb().setKb(config.getDouble("MaxKb"));
        }

        @Override
        public void onLaunch() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.WITHER_IDLE, 0.5f, 1);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.onHit(null);
        }

        @Override
        public void onTick() {

            if (this.ticksAlive % 2 == 0) {
                display(this.entity.getLocation(), true, this.config);
            }

            if (this.ticksAlive % 5 == 0) {
                this.entity.getWorld().playSound(this.entity.getLocation(), Sound.WITHER_SHOOT, 0.5f, 1);
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.onHit(target);
        }

        private void onHit(LivingEntity avoid) {
            Location loc = this.entity.getLocation();

            double radius = this.config.getDouble("Radius");

            this.entity.getWorld().playSound(loc, Sound.EXPLODE, 1.5f, 1);

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
            new ParticleMaker(particle).solidSphere(loc, radius / 2, 40, 0.1);

            EntityFinder finder = new EntityFinder(new DistanceSelector(radius));

            if (avoid != null) {
                finder.avoid(avoid);
            }

            finder.findAll(this.launcher, loc).forEach(target -> {
                double distanceSq = target.getLocation().distanceSquared(loc);
                double damage = YamlReader.getDecreasingValue(this.config, "Damage", distanceSq, radius * radius);
                double kb = YamlReader.getDecreasingValue(this.config, "Kb", distanceSq, radius * radius);

                Vector direction = VectorUtils.fromTo(this.entity, target);

                Attack settings = new Attack(this.config, direction);
                settings.getDamage().setDamage(damage);
                settings.getKb().setKb(kb);

                SSL.getInstance().getDamageManager().attack(target, this.ability, settings);
            });
        }
    }

    @Getter
    public static class RasenshurikenDisplayEvent extends CustomEvent {
        private final Rasenshuriken rasenshuriken;

        protected RasenshurikenDisplayEvent(Rasenshuriken rasenshuriken) {
            this.rasenshuriken = rasenshuriken;
        }
    }
}
