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
import com.github.zilosz.ssl.utils.file.YamlReader;
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
    protected final SSL plugin;

    @Getter protected final Ability ability;
    protected final Section config;
    @Getter protected Player launcher;
    @Getter protected T entity;
    @Setter protected Location overrideLocation;
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
    @Getter protected boolean invisible;
    @Getter protected AttackSettings attackSettings;

    @Getter protected int ticksAlive = 0;
    protected Vector launchVelocity;
    @Getter protected double launchSpeed;
    protected int timesBounced = 0;

    public CustomProjectile(SSL plugin, Ability ability, Section config) {
        this.plugin = plugin;
        this.ability = ability;
        this.config = config;

        this.launcher = ability.getPlayer();
        this.attackSettings = new AttackSettings(config, null);

        this.spread = config.getFloat("Spread");
        this.lifespan = config.getOptionalInt("Lifespan").orElse(Integer.MAX_VALUE);
        this.hasGravity = config.getOptionalBoolean("HasGravity").orElse(true);
        this.maxBounces = config.getInt("MaxBounces");
        this.hitBox = config.getOptionalDouble("HitBox").orElse(this.defaultHitBox());
        this.hitsMultiple = config.getBoolean("HitsMultiple");
        this.removeOnEntityHit = config.getOptionalBoolean("RemoveOnEntityHit").orElse(true);
        this.removeOnBlockHit = config.getOptionalBoolean("RemoveOnBlockHit").orElse(true);
        this.distanceFromEye = config.getOptionalDouble("DistanceFromEye").orElse(1.0);
        this.invisible = config.getBoolean("Invisible");
    }

    public double defaultHitBox() {
        return 0.8;
    }

    public Vector getLaunchVelocity() {
        return this.launchVelocity.clone();
    }

    @SuppressWarnings("unchecked")
    public CustomProjectile<T> copy(Ability ability) {
        return (CustomProjectile<T>) Reflector.newInstance(this.getClass(), this.plugin, ability, this.config);
    }

    public void launch() {
        this.speed = this.speed == null ? this.config.getDouble("Speed") : this.speed;

        ProjectileLaunchEvent projectileLaunchEvent = new ProjectileLaunchEvent(this, this.speed);
        Bukkit.getPluginManager().callEvent(projectileLaunchEvent);

        if (projectileLaunchEvent.isCancelled()) return;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        Location location = this.overrideLocation == null ? this.ability
                .getPlayer()
                .getEyeLocation() : this.overrideLocation.clone();
        location.setDirection(VectorUtils.getRandomVectorInDirection(location, this.spread));

        location.add(location.getDirection().multiply(this.distanceFromEye));

        this.launchSpeed = projectileLaunchEvent.getSpeed();
        this.launchVelocity = location.getDirection().multiply(this.launchSpeed);

        this.entity = this.createEntity(location);
        this.applyEntityParams();
        this.entity.setVelocity(this.launchVelocity);

        this.config.getOptionalSection("LaunchSound")
                .ifPresent(soundConfig -> YamlReader.noise(soundConfig).playForAll(location));

        this.runTaskTimer(this.plugin, 0, 0);
        this.onLaunch();
    }

    public abstract T createEntity(Location location);

    private void applyEntityParams() {
        if (this.invisible) {
            NmsUtils.broadcastPacket(new PacketPlayOutEntityDestroy(1, this.entity.getEntityId()));
        }
    }

    public void onLaunch() {}

    protected void handleBlockHitResult(BlockHitResult result) {
        if (result == null) return;

        ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, result);
        Bukkit.getPluginManager().callEvent(event);

        this.onBlockHit(result);

        this.config.getOptionalSection("BlockHitSound")
                .ifPresent(section -> YamlReader.noise(section).playForAll(this.entity.getLocation()));

        if (result.getFace() == null) return;

        ++this.timesBounced;
        if (this.timesBounced > this.maxBounces) {

            if (this.removeOnBlockHit) {
                this.remove(ProjectileRemoveReason.HIT_BLOCK);
            }

            return;
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

        if (this instanceof ActualProjectile) {
            this.entity = this.createEntity(this.entity.getLocation());
            this.applyEntityParams();
        }

        this.setVelocity(velocity);
    }

    public void onBlockHit(BlockHitResult result) {}

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

    public void setVelocity(Vector velocity) {
        if (this.hasGravity) {
            this.entity.setVelocity(velocity);
        } else {
            this.launchVelocity = velocity;
        }
    }

    public void onRemove(ProjectileRemoveReason reason) {}

    @Override
    public void run() {
        this.ticksAlive++;
        ProjectileRemoveReason reason = null;

        if (!this.entity.isValid()) {
            reason = ProjectileRemoveReason.ENTITY_DEATH;

        } else if (!(this.plugin.getGameManager().getState() instanceof InGameState)) {
            reason = ProjectileRemoveReason.DEACTIVATION;

        } else if (this.ticksAlive >= this.lifespan) {
            reason = ProjectileRemoveReason.LIFESPAN;
        }

        if (reason != null) {
            this.remove(reason);
            return;
        }

        if (!(this instanceof ActualProjectile) || this.config.isNumber("HitBox")) {
            this.searchForHit();
        }

        if (!this.hasGravity) {
            this.entity.setVelocity(this.launchVelocity);
        }

        this.onTick();
    }

    protected void searchForHit() {
        EntityFinder finder = this.getFinder();

        if (this.hitsMultiple) {
            finder.findAll(this.launcher, this.entity.getLocation()).forEach(this::handleTargetHit);

        } else {
            finder.findClosest(this.launcher, this.entity.getLocation()).ifPresent(this::handleTargetHit);
        }
    }

    public void onTick() {}

    protected EntityFinder getFinder() {
        return new EntityFinder(this.plugin, new HitBoxSelector(this.hitBox));
    }

    protected void handleTargetHit(LivingEntity target) {
        this.attackSettings.modifyKb(kb -> kb.setDirection(this.entity.getVelocity()));

        if (!this.plugin.getDamageManager().attack(target, this.ability, this.attackSettings)) return;

        this.launcher.playSound(this.launcher.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
        this.config.getOptionalSection("TargetHitSound")
                .ifPresent(sound -> YamlReader.noise(sound).playForAll(this.entity.getLocation()));

        this.onTargetHit(target);

        if (this.removeOnEntityHit) {
            this.remove(ProjectileRemoveReason.HIT_ENTITY);
        }
    }

    public void onTargetHit(LivingEntity target) {}
}
