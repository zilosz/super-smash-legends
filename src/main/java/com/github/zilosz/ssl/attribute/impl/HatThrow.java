package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.EulerAngle;

public class HatThrow extends RightClickAbility {
  private HatProjectile hatProjectile;

  @Override
  public void onClick(PlayerInteractEvent event) {

    if (hatProjectile == null || hatProjectile.state == State.INACTIVE) {
      hatProjectile = new HatProjectile(config, new AttackInfo(AttackType.HAT_THROW, this));
      hatProjectile.launch();
    }
    else if (hatProjectile.state == State.DISMOUNTED) {
      hatProjectile.mount(player);
    }
    else {
      hatProjectile.dismount();
    }
  }

  public enum State {
    INACTIVE, DISMOUNTED, MOUNTED
  }

  private static final class HatProjectile extends LivingProjectile<ArmorStand> {
    private State state = State.INACTIVE;

    public HatProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public ArmorStand createEntity(Location location) {
      ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
      stand.setArms(true);
      stand.setCanPickupItems(false);
      stand.setItemInHand(YamlReader.stack(config.getSection("HatItem")));
      stand.setMarker(true);
      stand.setVisible(false);
      return stand;
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      entity.getWorld().playSound(entity.getLocation(), Sound.ZOMBIE_WOODBREAK, 2, 2);
    }

    @Override
    public void onTick() {
      entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_PICKUP, 1, 1);
      entity.setVelocity(launchVelocity.clone().multiply(speedFunction(ticksAlive)));

      double height = ticksAlive * config.getDouble("RotationPerTick");
      entity.setRightArmPose(new EulerAngle(0, height, 0));
    }

    private double speedFunction(int ticks) {
      return -2 * ticks * speed / lifespan + speed;
    }

    @Override
    public void onLaunch() {
      state = State.DISMOUNTED;
    }

    @Override
    public void onRemove(ProjectileRemoveReason reason) {
      state = State.INACTIVE;
      ((ClickableAbility) attackInfo.getAttribute()).startCooldown();
    }

    public void mount(Entity passenger) {
      state = State.MOUNTED;
      entity.setPassenger(passenger);
      entity.getWorld().playSound(entity.getLocation(), Sound.CLICK, 2, 1);
    }

    public void dismount() {
      state = State.DISMOUNTED;
      entity.eject();
      entity.getWorld().playSound(entity.getLocation(), Sound.CLICK, 2, 1);
    }
  }
}
