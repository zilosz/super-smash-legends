package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import com.github.zilosz.ssl.util.message.Chat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BatWave extends RightClickAbility {
  private final List<BatProjectile> bats = new ArrayList<>();
  private State state = State.INACTIVE;
  private BukkitTask resetTask;
  private boolean hasSlinged;

  @Override
  public void onClick(PlayerInteractEvent event) {

    switch (state) {

      case INACTIVE:
        launch();
        break;

      case UNLEASHED:
        leash();
        break;

      case LEASHED:
        unleash();
    }
  }

  private void launch() {
    state = State.UNLEASHED;
    sendUseMessage();

    Location center = player.getEyeLocation();

    double width = config.getDouble("Width");
    double height = config.getDouble("Height");
    int count = config.getInt("BatCount");

    List<Location> locations = VectorUtils.flatRectLocations(center, width, height, count, true);
    AttackInfo attackInfo = new AttackInfo(AttackType.BAT_WAVE, this);
    locations.forEach(loc -> addAndLaunch(new BatProjectile(config, attackInfo), loc));

    resetTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
      reset();
      startCooldown();
    }, config.getInt("Lifespan"));
  }

  private void addAndLaunch(BatProjectile projectile, Location location) {
    bats.add(projectile);
    projectile.setOverrideLocation(location);
    projectile.launch();
  }

  private void reset() {
    state = State.INACTIVE;
    hasSlinged = false;
    CollectionUtils.clearWhileIterating(bats,
        bat -> bat.remove(ProjectileRemoveReason.DEACTIVATION)
    );

    if (resetTask != null) {
      resetTask.cancel();
    }
  }

  private void leash() {
    bats.forEach(BatProjectile::leash);
    state = State.LEASHED;
  }

  private void unleash() {
    bats.forEach(BatProjectile::unleash);
    state = State.UNLEASHED;
  }

  @Override
  public void run() {
    super.run();

    if (state == State.LEASHED) {
      player.setVelocity(bats.get(0).getLaunchVelocity());
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @EventHandler
  public void onDropItem(PlayerDropItemEvent event) {
    if (event.getPlayer() != player) return;
    if (state != State.LEASHED) return;
    if (hasSlinged) return;

    hasSlinged = true;

    Vector slingVector = VectorUtils.fromTo(player, bats.get(0).getEntity());
    slingVector.normalize().multiply(config.getDouble("SlingVelocity"));
    player.setVelocity(slingVector);

    player.getWorld().playSound(player.getLocation(), Sound.MAGMACUBE_JUMP, 2, 1);
    Chat.ABILITY.send(player, "&7You threw yourself like a slingshot!");

    if (state == State.LEASHED) {
      unleash();
    }
  }

  private enum State {
    INACTIVE, LEASHED, UNLEASHED
  }

  private static class BatProjectile extends LivingProjectile<ArmorStand> {
    private Bat bat;

    public BatProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public ArmorStand createEntity(Location location) {
      ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
      stand.setVisible(false);
      stand.setMarker(true);

      bat = location.getWorld().spawn(location, Bat.class);
      stand.setPassenger(bat);

      return stand;
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      entity.getWorld().playSound(entity.getLocation(), Sound.BAT_DEATH, 1, 1);
    }

    @Override
    public void onLaunch() {
      SSL.getInstance().getTeamManager().addEntityToTeam(bat, launcher);
    }

    @Override
    public void onRemove(ProjectileRemoveReason reason) {
      super.onRemove(reason);
      unleash();
      bat.remove();
      SSL.getInstance().getTeamManager().removeEntityFromTeam(bat);
    }

    public void unleash() {
      bat.setLeashHolder(null);
    }

    public void leash() {
      bat.setLeashHolder(launcher);
    }

    @EventHandler
    public void onDamageBat(DamageEvent event) {
      if (event.getVictim() == bat) {
        event.setCancelled(true);
      }
    }
  }
}
