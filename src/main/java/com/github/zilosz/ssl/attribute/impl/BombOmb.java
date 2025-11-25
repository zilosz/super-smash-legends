package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class BombOmb extends RightClickAbility {
  private BombProjectile bombProjectile;

  @Override
  public void onClick(PlayerInteractEvent event) {

    if (bombProjectile == null || bombProjectile.state == State.INACTIVE) {
      sendUseMessage();

      AttackInfo attackInfo = new AttackInfo(AttackType.BOMB_OMB_DIRECT, this);
      bombProjectile = new BombProjectile(config.getSection("Projectile"), attackInfo);
      bombProjectile.launch();
    }
    else if (bombProjectile.state == State.THROWN) {
      bombProjectile.solidify();
    }
    else if (bombProjectile.canExplode) {
      bombProjectile.explode();
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (bombProjectile != null) {
      bombProjectile.destroy();
    }
  }

  public enum State {
    INACTIVE, THROWN, WAITING
  }

  private static class BombProjectile extends ItemProjectile {
    private State state = State.INACTIVE;
    private Block bombBlock;
    private BukkitTask soundTask;
    private BukkitTask explodeTask;
    private boolean missed = true;
    private boolean canExplode;

    public BombProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onLaunch() {
      state = State.THROWN;
      entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_PICKUP, 2, 0.5f);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      if (missed) {
        solidify();
      }
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      missed = false;
      solidify();
    }

    @Override
    public void onTick() {
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
      new ParticleMaker(particle).show(entity.getLocation());
    }

    private void solidify() {
      remove(ProjectileRemoveReason.CUSTOM);

      state = State.WAITING;

      bombBlock = entity.getLocation().getBlock();
      bombBlock.setType(Material.COAL_BLOCK);

      soundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
        entity.getWorld().playSound(bombBlock.getLocation(), Sound.FUSE, 1, 1);
      }, 0, 0);

      explodeTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
        if (state == State.WAITING) {
          explode();
        }
      }, config.getInt("Explode.Delay"));

      int disableTicks = config.getInt("Explode.DisableTicks");
      Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> canExplode = true, disableTicks);
    }

    private void explode() {
      state = State.INACTIVE;
      bombBlock.setType(Material.AIR);

      soundTask.cancel();
      bombBlock.getWorld().playSound(bombBlock.getLocation(), Sound.EXPLODE, 2, 1);

      for (int i = 0; i < 3; i++) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
        new ParticleMaker(particle).show(bombBlock.getLocation());
      }

      new EntityFinder(new DistanceSelector(config.getDouble("Explode.Range")))
          .setTeamPreference(TeamPreference.ANY)
          .setAvoidsUser(false)
          .findAll(launcher, bombBlock.getLocation())
          .forEach(this::attemptExplodeHit);

      ((ClickableAbility) attackInfo.getAttribute()).startCooldown();
    }

    private void attemptExplodeHit(LivingEntity target) {
      Section explode = config.getSection("Explode");

      double max = explode.getDouble("Range") * explode.getDouble("Range");
      double distanceSq = bombBlock.getLocation().distanceSquared(target.getLocation());
      double damage = YamlReader.decreasingValue(explode, "Damage", distanceSq, max);
      double kb = YamlReader.decreasingValue(explode, "Kb", distanceSq, max);

      Vector direction = VectorUtils.fromTo(bombBlock.getLocation(), target.getLocation());

      String name = ((Ability) attackInfo.getAttribute()).getDisplayName();
      Attack attack = YamlReader.attack(explode, direction, name);
      attack.getDamage().setDamage(damage);
      attack.getKb().setKb(kb);

      AttackInfo explodeInfo =
          new AttackInfo(AttackType.BOMB_OMB_EXPLOSION, attackInfo.getAttribute());
      SSL.getInstance().getDamageManager().attack(target, attack, explodeInfo);
    }

    private void destroy() {
      remove(ProjectileRemoveReason.DEACTIVATION);

      if (explodeTask != null) {
        explodeTask.cancel();
        soundTask.cancel();
        bombBlock.setType(Material.AIR);
      }
    }
  }
}
