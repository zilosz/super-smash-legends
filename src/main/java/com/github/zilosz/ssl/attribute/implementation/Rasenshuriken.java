package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
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
import org.bukkit.entity.Player;
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
    private int ticksCharged = -1;

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 1);
        this.hotbarItem.hide();

        this.task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (++this.ticksCharged >= this.config.getInt("Lifespan")) {
                this.reset();
                this.startCooldown();
                return;
            }

            if (this.ticksCharged % 2 == 0) {
                this.displayOnHead(this.player);
                Bukkit.getPluginManager().callEvent(new RasenshurikenDisplayEvent(this));
            }

            if (this.ticksCharged % 7 == 0) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.FUSE, 1, 1);
            }
        }, 0, 0);
    }

    private void reset() {
        if (this.ticksCharged == -1) return;

        this.ticksCharged = -1;
        this.task.cancel();
        this.hotbarItem.show();

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIRE_IGNITE, 3, 2);
    }

    public void displayOnHead(Player entity) {
        display(this.getHeadLocation(entity), false);
    }

    public static void display(Location location, boolean tilted) {
        float pitch = 90;
        float yaw = location.getYaw();

        if (tilted) {
            float actual = location.getPitch();
            pitch = actual >= 0 ? actual - 90 : actual + 90;
        }

        for (double radius = 0; radius <= 0.5; radius += 0.16) {
            ParticleBuilder outerParticle = new ParticleBuilder(ParticleEffect.REDSTONE)
                    .setColor(new Color(255, 255, 255));
            new ParticleMaker(outerParticle).ring(location, pitch, yaw, radius, 18);
        }

        ParticleBuilder innerParticle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(173, 216, 230));
        new ParticleMaker(innerParticle).hollowSphere(location, 0.3, 5);
    }

    private Location getHeadLocation(Player entity) {
        return EntityUtils.top(entity).add(0, this.config.getDouble("Height"), 0);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getPlayer() != this.player) return;
        if (this.ticksCharged == -1) return;

        Bukkit.getPluginManager().callEvent(new RasenshurikenLaunchEvent(this));
        this.launch(this.player, this, AttackType.RASENSHURIKEN);

        this.reset();
        this.startCooldown();
    }

    public Shuriken launch(Player entity, Ability owningAbility, AttackType attackType) {
        Section config = this.config.getSection("Projectile");
        Shuriken shuriken = new Shuriken(config, new AttackInfo(attackType, owningAbility));
        Vector direction = entity.getEyeLocation().getDirection();
        shuriken.setOverrideLocation(this.getHeadLocation(entity).setDirection(direction));
        shuriken.launch();
        return shuriken;
    }

    @EventHandler
    public void onHandSwitch(PlayerItemHeldEvent event) {
        if (event.getPlayer() == this.player && event.getNewSlot() != this.slot && this.ticksCharged > -1) {
            this.reset();
            this.startCooldown();
        }
    }

    public static class Shuriken extends ItemProjectile {

        public Shuriken(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
            this.attack.getDamage().setDamage(config.getDouble("MaxDamage"));
            this.attack.getKb().setKb(config.getDouble("MaxKb"));
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
                Rasenshuriken.display(this.entity.getLocation(), true);
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
                double damage = YamlReader.decreasingValue(this.config, "Damage", distanceSq, radius * radius);
                double kb = YamlReader.decreasingValue(this.config, "Kb", distanceSq, radius * radius);

                Vector direction = VectorUtils.fromTo(this.entity, target);
                String name = ((Ability) this.attackInfo.getAttribute()).getDisplayName();
                Attack attack = YamlReader.attack(this.config, direction, name);
                attack.getDamage().setDamage(damage);
                attack.getKb().setKb(kb);

                SSL.getInstance().getDamageManager().attack(target, attack, this.attackInfo);
            });
        }
    }

    @Getter
    private static class RasenshurikenEvent extends CustomEvent {
        private final Rasenshuriken rasenshuriken;

        public RasenshurikenEvent(Rasenshuriken rasenshuriken) {
            this.rasenshuriken = rasenshuriken;
        }
    }

    public static class RasenshurikenDisplayEvent extends RasenshurikenEvent {

        public RasenshurikenDisplayEvent(Rasenshuriken rasenshuriken) {
            super(rasenshuriken);
        }
    }

    public static class RasenshurikenLaunchEvent extends RasenshurikenEvent {

        public RasenshurikenLaunchEvent(Rasenshuriken rasenshuriken) {
            super(rasenshuriken);
        }
    }
}
