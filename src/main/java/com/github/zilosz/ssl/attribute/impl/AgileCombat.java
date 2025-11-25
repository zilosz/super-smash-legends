package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attribute.DoubleJumpEvent;
import com.github.zilosz.ssl.util.effects.Effects;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class AgileCombat extends RightClickAbility {
  private State state = State.INACTIVE;
  private BukkitTask dropTask;
  private BukkitTask leapTask;
  private BukkitTask cancelTask;
  private boolean canLeap = true;

  @Override
  public void onClick(PlayerInteractEvent event) {

    switch (state) {

      case INACTIVE:
        sendUseMessage();
        onFirstClick();
        break;

      case SPRINTING:
        if (canLeap) {
          onLeap();
        }
    }
  }

  private void onFirstClick() {
    player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 2);

    FireworkEffect.Builder settings = FireworkEffect
        .builder()
        .withColor(kit.getColor().getBukkitColor())
        .with(FireworkEffect.Type.BURST)
        .trail(true);

    Effects.launchFirework(EntityUtils.top(player), settings, 1);

    player.setVelocity(new Vector(0, -config.getDouble("DropVelocity"), 0));
    player.setWalkSpeed(config.getFloat("WalkSpeed"));

    state = State.DROPPING;

    dropTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
      if (EntityUtils.isPlayerGrounded(player)) {
        dropTask.cancel();
        state = State.SPRINTING;
      }
    }, 0, 0);

    int duration = config.getInt("Duration");
    cancelTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> reset(true), duration);
  }

  private void reset(boolean natural) {
    if (state == State.INACTIVE) return;

    state = State.INACTIVE;
    canLeap = true;
    player.setWalkSpeed(0.2f);

    if (cancelTask != null) {
      cancelTask.cancel();
    }

    if (dropTask != null) {
      dropTask.cancel();
    }

    if (leapTask != null) {
      leapTask.cancel();
    }

    if (natural) {
      startCooldown();
      player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 2);
    }
  }

  private void onLeap() {
    state = State.LEAPING;

    double velocity = config.getDouble("Leap.Velocity");
    player.setVelocity(player.getEyeLocation().getDirection().multiply(velocity));

    player.getWorld().playSound(player.getLocation(), Sound.WITHER_IDLE, 1, 2);

    leapTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (EntityUtils.isPlayerGrounded(player)) {
        endLeap(false);
        return;
      }

      EntitySelector selector = new HitBoxSelector(config.getDouble("Leap.HitBox"));

      new EntityFinder(selector).findClosest(player).ifPresent(target -> {
        Vector direction = player.getEyeLocation().getDirection();
        Attack attack = YamlReader.attack(config.getSection("Leap"), direction, getDisplayName());
        AttackInfo attackInfo = new AttackInfo(AttackType.AGILE_COMBAT, this);

        if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
          endLeap(true);

          double springVel = config.getDouble("SpringVelocity");
          double springY = config.getDouble("SpringVelocityY");
          player.setVelocity(direction.multiply(springVel).setY(springY));

          player.getWorld().playSound(player.getLocation(), Sound.HORSE_GALLOP, 3, 1);
          player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1, 2);
        }
      });
    }, 4, 0);
  }

  private void endLeap(boolean canLeap) {
    this.canLeap = canLeap;
    state = State.SPRINTING;
    leapTask.cancel();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset(false);
  }

  @EventHandler
  public void onAttack(AttackEvent event) {
    if (event.getVictim() == player && state == State.DROPPING) {
      reset(true);
    }
  }

  @EventHandler
  public void onJump(DoubleJumpEvent event) {
    if (event.getPlayer() == player && state.boostsJump) {
      Section jumpConfig = config.getSection("Jump");
      event.setPower(jumpConfig.getDouble("Power"));
      event.setHeight(jumpConfig.getDouble("Height"));
      event.setNoise(YamlReader.noise(jumpConfig.getSection("Sound")));
    }
  }

  private enum State {
    INACTIVE(false), DROPPING(false), LEAPING(true), SPRINTING(true);

    private final boolean boostsJump;

    State(boolean boostsJump) {
      this.boostsJump = boostsJump;
    }
  }
}
