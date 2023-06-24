package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.event.projectile.ProjectileHitBlockEvent;
import com.github.zilosz.ssl.event.projectile.ProjectileLaunchEvent;
import com.github.zilosz.ssl.event.projectile.ProjectileRemoveEvent;
import com.github.zilosz.ssl.game.state.InGameState;
import com.github.zilosz.ssl.utils.NmsUtils;
import com.github.zilosz.ssl.utils.Reflector;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public abstract class CustomProjectile<T extends Entity> extends BukkitRunnable implements Listener {
    protected final Section config;

    @Getter protected final Ability ability;
    @Getter protected Player launcher;
    @Getter protected T entity;
    @Getter protected boolean invisible;
    @Getter protected AttackSettings attackSettings;
    @Getter protected int ticksAlive = 0;
    @Getter protected double launchSpeed;

    @Getter @Setter protected Double speed;
    @Getter @Setter protected float spread;
    @Getter @Setter protected int lifespan;
    @Getter @Setter protected boolean hasGravity;
    @Getter @Setter protected int maxBounces;
    @Getter @Setter protected double hitBox;
    @Getter @Setter protected boolean hitsMultiple;
    @Getter @Setter protected boolean removeOnEntityHit;
    @Getter @Setter protected double distanceFromEye;
    @Getter @Setter protected boolean removeOnBlockHit;

    @Setter protected Location overrideLocation;

    protected Vector launchVelocity;
    protected int timesBounced = 0;
    protected double defaultHitBox = 0.8;
    protected boolean recreateOnBounce = false;
    protected boolean useCustomHitBox = true;
    protected EntityFinder entityFinder;

    public CustomProjectile(Ability ability, Section config) {
        this.ability = ability;
        this.config = config;

        this.launcher = ability.getPlayer();
        this.attackSettings = new AttackSettings(config, null);

        this.spread = config.getFloat("Spread");
        this.lifespan = config.getOptionalInt("Lifespan").orElse(Integer.MAX_VALUE);
        this.hasGravity = config.getOptionalBoolean("HasGravity").orElse(true);
        this.maxBounces = config.getInt("MaxBounces");
        this.hitBox = config.getOptionalDouble("HitBox").orElse(this.defaultHitBox);
        this.hitsMultiple = config.getBoolean("HitsMultiple");
        this.removeOnEntityHit = config.getOptionalBoolean("RemoveOnEntityHit").orElse(true);
        this.removeOnBlockHit = config.getOptionalBoolean("RemoveOnBlockHit").orElse(true);
        this.distanceFromEye = config.getOptionalDouble("DistanceFromEye").orElse(1.0);
        this.invisible = config.getBoolean("Invisible");

        this.entityFinder = new EntityFinder(new HitBoxSelector(this.hitBox));
    }

    public Vector getLaunchVelocity() {
        return this.launchVelocity.clone();
    }

    @SuppressWarnings("unchecked")
    public CustomProjectile<T> copy(Ability ability) {
        return (CustomProjectile<T>) Reflector.newInstance(this.getClass(), SSL.getInstance(), ability, this.config);
    }

    public void launch() {
        this.speed = this.speed == null ? this.config.getDouble("Speed") : this.speed;

        ProjectileLaunchEvent projectileLaunchEvent = new ProjectileLaunchEvent(this, this.speed);
        Bukkit.getPluginManager().callEvent(projectileLaunchEvent);

        if (projectileLaunchEvent.isCancelled()) return;

        Location eyeLoc = this.ability.getPlayer().getEyeLocation();
        Location location = this.overrideLocation == null ? eyeLoc : this.overrideLocation.clone();
        location.setDirection(VectorUtils.getRandomVectorInDirection(location, this.spread));
        location.add(location.getDirection().multiply(this.distanceFromEye));

        this.launchSpeed = projectileLaunchEvent.getSpeed();
        this.launchVelocity = location.getDirection().multiply(this.launchSpeed);

        this.entity = this.createEntity(location);
        this.applyEntityParams();
        this.entity.setVelocity(this.launchVelocity);

        this.runTaskTimer(SSL.getInstance(), 0, 0);
        Bukkit.getPluginManager().registerEvents(this, SSL.getInstance());

        this.onLaunch();
    }

    protected abstract T createEntity(Location location);

    private void applyEntityParams() {
        if (this.invisible) {
            NmsUtils.broadcastPacket(new PacketPlayOutEntityDestroy(1, this.entity.getEntityId()));
        }
    }

    protected void onLaunch() {}

    protected void hitBlock(BlockHitResult result) {
        if (result == null) return;

        ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, result);
        Bukkit.getPluginManager().callEvent(event);

        this.onBlockHit(result);

        if (result.getFace() != null) {

            if (++this.timesBounced > this.maxBounces) {
                this.remove(ProjectileRemoveReason.HIT_BLOCK);
            }

            Vector velocity = this.hasGravity ? this.launchVelocity : this.entity.getVelocity();

            switch (result.getFace()) {

                case UP:
                case DOWN:
                    velocity.setY(-velocity.getY());
                    break;

                case NORTH:
                case SOUTH:
                    velocity.setZ(-velocity.getZ());
                    break;

                default:
                    velocity.setX(-velocity.getX());
            }

            if (this.recreateOnBounce) {
                this.entity = this.createEntity(this.entity.getLocation());
                this.applyEntityParams();
            }

            this.setVelocity(velocity);
        }
    }

    protected void onBlockHit(BlockHitResult result) {}

    public void remove(ProjectileRemoveReason reason) {
        ProjectileRemoveEvent event = new ProjectileRemoveEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            HandlerList.unregisterAll(this);
            this.entity.remove();
            this.cancel();
            this.onRemove(reason);
        }
    }

    protected void setVelocity(Vector velocity) {
        if (this.hasGravity) {
            this.entity.setVelocity(velocity);
        } else {
            this.launchVelocity = velocity;
        }
    }

    protected void onRemove(ProjectileRemoveReason reason) {}

    @Override
    public void run() {
        this.ticksAlive++;
        ProjectileRemoveReason reason = null;

        if (!this.entity.isValid()) {
            reason = ProjectileRemoveReason.ENTITY_DEATH;

        } else if (!(SSL.getInstance().getGameManager().getState() instanceof InGameState)) {
            reason = ProjectileRemoveReason.DEACTIVATION;

        } else if (this.ticksAlive >= this.lifespan) {
            reason = ProjectileRemoveReason.LIFESPAN;
        }

        if (reason != null) {
            this.remove(reason);
            return;
        }

        if (!this.useCustomHitBox || this.config.isNumber("HitBox")) {
            this.searchForHit();
        }

        if (!this.hasGravity) {
            this.entity.setVelocity(this.launchVelocity);
        }

        this.onTick();
    }

    private void searchForHit() {
        Location loc = this.entity.getLocation();

        if (this.hitsMultiple) {
            this.entityFinder.findAll(this.launcher, loc).forEach(this::hitTarget);

        } else {
            this.entityFinder.findClosest(this.launcher, loc).ifPresent(this::hitTarget);
        }
    }

    protected void onTick() {}

    protected void hitTarget(LivingEntity target) {
        this.attackSettings.modifyKb(kb -> kb.setDirection(this.entity.getVelocity()));

        if (SSL.getInstance().getDamageManager().attack(target, this.ability, this.attackSettings)) {
            this.onTargetHit(target);
            this.launcher.playSound(this.launcher.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);

            if (this.removeOnEntityHit) {
                this.remove(ProjectileRemoveReason.HIT_ENTITY);
            }
        }
    }

    protected void onTargetHit(LivingEntity target) {}
}
