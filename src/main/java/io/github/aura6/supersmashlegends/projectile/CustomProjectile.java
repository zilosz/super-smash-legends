package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.ProjectileLaunchEvent;
import io.github.aura6.supersmashlegends.utils.NmsUtils;
import io.github.aura6.supersmashlegends.utils.Reflector;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
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
    protected final SuperSmashLegends plugin;

    @Getter protected final Ability ability;
    @Getter protected Player launcher;
    @Getter protected T entity;
    protected final Section config;

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
    @Getter protected Damage damage;

    @Getter protected int ticksAlive = 0;
    protected Vector constantVelocity;
    @Getter protected double launchSpeed;
    protected int timesBounced = 0;

    public CustomProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        this.plugin = plugin;
        this.ability = ability;
        this.config = config;

        launcher = ability.getPlayer();
        damage = Damage.Builder.fromConfig(config, null).setKbY(0.5).build();

        spread = config.getFloat("Spread");
        lifespan = config.getOptionalInt("Lifespan").orElse(Integer.MAX_VALUE);
        hasGravity = config.getOptionalBoolean("HasGravity").orElse(true);
        maxBounces = config.getInt("MaxBounces");
        hitBox = config.getOptionalDouble("HitBox").orElse(defaultHitBox());
        hitsMultiple = config.getBoolean("HitsMultiple");
        removeOnEntityHit = config.getOptionalBoolean("RemoveOnEntityHit").orElse(true);
        removeOnBlockHit = config.getOptionalBoolean("RemoveOnBlockHit").orElse(true);
        distanceFromEye = config.getOptionalDouble("DistanceFromEye").orElse(1.0);
        invisible = config.getBoolean("Invisible");
    }

    @SuppressWarnings("unchecked")
    public CustomProjectile<T> copy(Ability ability) {
        return (CustomProjectile<T>) Reflector.newInstance(getClass(), this.plugin, ability, this.config);
    }

    public double defaultHitBox() {
        return 0.75;
    }

    public abstract T createEntity(Location location);

    private void applyEntityParams() {
        if (invisible) {
            NmsUtils.broadcastPacket(new PacketPlayOutEntityDestroy(1, entity.getEntityId()));
        }
    }

    public void onLaunch() {}

    public void launch() {
        speed = this.speed == null ? config.getDouble("Speed") : this.speed;

        ProjectileLaunchEvent projectileLaunchEvent = new ProjectileLaunchEvent(this, speed);
        Bukkit.getPluginManager().callEvent(projectileLaunchEvent);

        if (projectileLaunchEvent.isCancelled()) return;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        Location location = overrideLocation == null ? ability.getPlayer().getEyeLocation() : overrideLocation.clone();
        location.setYaw((float) MathUtils.randSpread(location.getYaw(), spread));
        location.setPitch((float) MathUtils.randSpread(location.getPitch(), spread));

        location.add(location.getDirection().multiply(distanceFromEye));

        launchSpeed = projectileLaunchEvent.getSpeed();
        constantVelocity = location.getDirection().multiply(launchSpeed);

        entity = createEntity(location);
        applyEntityParams();
        entity.setVelocity(constantVelocity);

        config.getOptionalSection("LaunchSound").ifPresent(soundConfig -> YamlReader.noise(soundConfig).playForAll(location));

        runTaskTimer(plugin, 0, 0);
        onLaunch();
    }

    public void onRemove() {}

    public void remove() {
        entity.remove();
        cancel();
        HandlerList.unregisterAll(this);
        onRemove();
    }

    public void onBlockHit(BlockHitResult result) {}

    protected void handleBlockHitResult(BlockHitResult result) {
        if (result == null) return;

        onBlockHit(result);
        config.getOptionalSection("BlockHitSound").ifPresent(section -> YamlReader.noise(section).playForAll(entity.getLocation()));

        if (result.getFace() == null) return;

        if (++timesBounced > maxBounces) {

            if (removeOnBlockHit) {
                remove();
            }

            return;
        }

        Vector velocity = hasGravity ? constantVelocity : entity.getVelocity();

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
            entity = createEntity(entity.getLocation());
            applyEntityParams();
        }

        setVelocity(velocity);
    }

    public void onTick() {}

    public void onTargetHit(LivingEntity target) {}

    protected void handleTargetHit(LivingEntity target) {
        damage.setDirection(this.entity.getVelocity());

        if (!plugin.getDamageManager().attemptAttributeDamage(target, damage, ability)) return;

        launcher.playSound(launcher.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
        config.getOptionalSection("TargetHitSound").ifPresent(sound -> YamlReader.noise(sound).playForAll(entity.getLocation()));

        onTargetHit(target);

        if (removeOnEntityHit) {
            remove();
        }
    }

    protected EntityFinder getFinder() {
        return new EntityFinder(plugin, new HitBoxSelector(hitBox));
    }

    protected void searchForHit() {
        EntityFinder finder = getFinder();

        if (hitsMultiple) {
            finder.findAll(launcher, entity.getLocation()).forEach(this::handleTargetHit);

        } else {
            finder.findClosest(launcher, entity.getLocation()).ifPresent(this::handleTargetHit);
        }
    }

    @Override
    public void run() {

        if (!entity.isValid() || ticksAlive++ >= lifespan) {
            remove();
            return;
        }

        if (!(this instanceof ActualProjectile) || config.isNumber("HitBox")) {
            searchForHit();
        }

        if (!hasGravity) {
            entity.setVelocity(constantVelocity);
        }

        onTick();
    }

    public void setVelocity(Vector velocity) {
        if (hasGravity) {
            entity.setVelocity(velocity);
        } else {
            constantVelocity = velocity;
        }
    }
}
