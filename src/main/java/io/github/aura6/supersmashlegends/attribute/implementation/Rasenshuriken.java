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
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.DistanceSelector;
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
import org.bukkit.scheduler.BukkitTask;

public class Rasenshuriken extends RightClickAbility {
    private BukkitTask task;
    private Location lastLocation;
    private int ticksCharged = -1;

    public Rasenshuriken(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
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

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
        this.hotbarItem.hide();

        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

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

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getPlayer() != this.player || this.ticksCharged == -1) return;

        Shuriken shuriken = new Shuriken(this.plugin, this, this.config.getSection("Projectile"));
        this.lastLocation.setDirection(this.player.getEyeLocation().getDirection());
        shuriken.setOverrideLocation(this.lastLocation);
        shuriken.launch();

        this.reset(true);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == this.player && event.getNewSlot() != this.slot && this.ticksCharged > -1) {
            this.reset(true);
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

        double radius = config.getDouble("ParticleRadius");
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(173, 216, 230).solidSphere(loc, radius, 7, 0.1);
    }

    public static class Shuriken extends ItemProjectile {

        public Shuriken(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);

            this.getDamage().setDamage(config.getDouble("MaxDamage"));
            this.getDamage().setKb(config.getDouble("MaxKb"));
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
            this.onHit(target);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.onHit(null);
        }

        private void onHit(LivingEntity avoid) {
            Location loc = this.entity.getLocation();

            double radius = this.config.getDouble("Radius");

            this.entity.getWorld().playSound(loc, Sound.EXPLODE, 1.5f, 1);
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).solidSphere(loc, radius / 2, 40, 0.1);

            new EntityFinder(this.plugin, new DistanceSelector(radius)).findAll(this.launcher, loc).forEach(target -> {
                if (target == avoid) return;

                double distanceSq = target.getLocation().distanceSquared(loc);
                double damage = YamlReader.decLin(this.config, "Damage", distanceSq, radius * radius);
                double kb = YamlReader.decLin(this.config, "Kb", distanceSq, radius * radius);

                Damage dmg = Damage.Builder.fromConfig(this.config, VectorUtils.fromTo(this.entity, target)).build();
                dmg.setDamage(damage);
                dmg.setKb(kb);

                this.plugin.getDamageManager().attemptAttributeDamage(target, dmg, this.ability);
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
