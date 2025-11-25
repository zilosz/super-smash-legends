package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.event.projectile.ProjectileHitBlockEvent;
import com.github.zilosz.ssl.game.state.GameStateType;
import com.github.zilosz.ssl.util.NmsUtils;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
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

public abstract class CustomProjectile<T extends Entity> extends BukkitRunnable
    implements Listener {
  protected final Section config;
  @Getter protected AttackInfo attackInfo;
  @Getter protected Player launcher;
  @Getter protected T entity;
  @Getter protected Attack attack;
  @Getter @Setter protected Double speed;
  @Setter protected Location overrideLocation;
  @Setter protected float spread;
  @Setter protected int lifespan;
  protected boolean hasGravity;
  protected int maxBounces;
  protected double hitBox;
  protected boolean hitsMultiple;
  protected boolean removeOnEntityHit;
  protected double distanceFromEye;
  protected boolean removeOnBlockHit;
  protected boolean invisible;
  protected boolean recreateOnBounce;
  protected boolean useCustomHitBox = true;
  protected boolean removeOnFailedHit;
  protected int ticksAlive;
  protected Vector launchVelocity;
  protected int timesBounced;
  protected double defaultHitBox;
  protected EntityFinder entityFinder;

  public CustomProjectile(Section config, AttackInfo attackInfo) {
    this.config = config;
    this.attackInfo = attackInfo;
    launcher = this.attackInfo.getAttribute().getPlayer();

    attack = YamlReader.attack(this.config, null, "");

    if (this.attackInfo.getAttribute() instanceof Ability) {
      attack.setName(((Ability) this.attackInfo.getAttribute()).getDisplayName());
    }

    Section defaults = SSL.getInstance().getResources().getConfig().getSection("Projectile");

    if (this.config.isNumber("HitBox")) {
      hitBox = this.config.getDouble("HitBox");
    }
    else {
      hitBox = defaults.getDouble("HitBox");
    }

    if (this.config.getOptionalBoolean("HasLifespan").orElse(true)) {
      lifespan = config.getOptionalInt("Lifespan").orElse(defaults.getInt("Lifespan"));
    }

    spread = config.getFloat("Spread");
    hasGravity = config.getOptionalBoolean("HasGravity").orElse(true);
    maxBounces = config.getInt("MaxBounces");
    hitsMultiple = config.getBoolean("HitsMultiple");
    removeOnEntityHit = config.getOptionalBoolean("RemoveOnEntityHit").orElse(true);
    removeOnBlockHit = config.getOptionalBoolean("RemoveOnBlockHit").orElse(true);
    removeOnFailedHit = config.getBoolean("RemoveOnFailedHit");
    distanceFromEye = config.getOptionalDouble("DistanceFromEye").orElse(1.0);
    invisible = config.getBoolean("Invisible");

    entityFinder = new EntityFinder(new HitBoxSelector(hitBox));
  }

  public Vector getLaunchVelocity() {
    return launchVelocity.clone();
  }

  public void launch() {
    speed = speed == null ? config.getDouble("Speed") : speed;

    Location eyeLoc = launcher.getEyeLocation();
    Location location = overrideLocation == null ? eyeLoc : overrideLocation.clone();
    location.setDirection(VectorUtils.randomVectorInDirection(location, spread));
    location.add(location.getDirection().multiply(distanceFromEye));

    launchVelocity = location.getDirection().multiply(speed);

    entity = createEntity(location);
    applyEntityParams();
    entity.setVelocity(launchVelocity);

    runTaskTimer(SSL.getInstance(), 0, 0);
    Bukkit.getPluginManager().registerEvents(this, SSL.getInstance());

    onLaunch();
  }

  protected abstract T createEntity(Location location);

  private void applyEntityParams() {
    if (invisible) {
      NmsUtils.broadcastPacket(new PacketPlayOutEntityDestroy(1, entity.getEntityId()));
    }
  }

  protected void onLaunch() {}

  protected void hitBlock(BlockHitResult result) {
    if (result == null) return;

    ProjectileHitBlockEvent event = new ProjectileHitBlockEvent(this, result);
    Bukkit.getPluginManager().callEvent(event);

    onBlockHit(result);

    if (result.getFace() == null) return;

    if (++timesBounced > maxBounces) {

      if (removeOnBlockHit) {
        remove(ProjectileRemoveReason.HIT_BLOCK);
      }
    }
    else {
      Vector velocity = hasGravity ? launchVelocity : entity.getVelocity();

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

      if (recreateOnBounce) {
        entity = createEntity(entity.getLocation());
        applyEntityParams();
      }

      setVelocity(velocity);
    }
  }

  protected void onBlockHit(BlockHitResult result) {}

  public void remove(ProjectileRemoveReason reason) {
    HandlerList.unregisterAll(this);
    entity.remove();
    cancel();
    onRemove(reason);
  }

  protected void onRemove(ProjectileRemoveReason reason) {}

  protected void setVelocity(Vector velocity) {
    if (hasGravity) {
      entity.setVelocity(velocity);
    }
    else {
      launchVelocity = velocity;
    }
  }

  @Override
  public void run() {
    ticksAlive++;
    ProjectileRemoveReason reason = null;

    if (!entity.isValid()) {
      reason = ProjectileRemoveReason.ENTITY_DEATH;

    }
    else if (SSL.getInstance().getGameManager().getState().getType() != GameStateType.IN_GAME) {
      reason = ProjectileRemoveReason.DEACTIVATION;

    }
    else if (ticksAlive >= lifespan) {
      reason = ProjectileRemoveReason.LIFESPAN;
    }

    if (reason != null) {
      remove(reason);
      return;
    }

    if (useCustomHitBox) {
      searchForHit();
    }

    if (!hasGravity) {
      entity.setVelocity(launchVelocity);
    }

    onTick();
  }

  private void searchForHit() {
    Location loc = EntityUtils.center(entity);

    if (hitsMultiple) {
      entityFinder.findAll(launcher, loc).forEach(this::hitTarget);
    }
    else {
      entityFinder.findClosest(launcher, loc).ifPresent(this::hitTarget);
    }
  }

  protected void hitTarget(LivingEntity target) {
    onPreTargetHit(target);
    attack.getKb().setDirection(entity.getVelocity());

    if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
      onTargetHit(target);
      launcher.playSound(launcher.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);

      if (removeOnEntityHit) {
        remove(ProjectileRemoveReason.HIT_ENTITY);
      }
    }
    else if (removeOnFailedHit) {
      remove(ProjectileRemoveReason.HIT_ENTITY);
    }
  }

  protected void onPreTargetHit(LivingEntity target) {}

  protected void onTargetHit(LivingEntity target) {}

  protected void onTick() {}
}
