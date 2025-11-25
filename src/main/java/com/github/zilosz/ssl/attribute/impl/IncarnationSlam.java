package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.effects.Effects;
import com.github.zilosz.ssl.util.entity.DisguiseUtils;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class IncarnationSlam extends RightClickAbility {
  private BukkitTask task;
  private boolean active;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || active;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    active = true;

    MobDisguise disguise = new MobDisguise(DisguiseType.SLIME);
    ((SlimeWatcher) disguise.getWatcher()).setSize(config.getInt("SlimeSize"));
    DisguiseAPI.disguiseToAll(player, DisguiseUtils.applyDisguiseParams(player, disguise));

    Vector direction = player.getEyeLocation().getDirection();
    player.setVelocity(direction.multiply(config.getDouble("Velocity")));

    player.getWorld().playSound(player.getLocation(), Sound.SLIME_WALK, 3, 0.75f);

    task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (EntityUtils.isPlayerGrounded(player)) {
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 2);
        DisguiseAPI.undisguiseToAll(player);
        startCooldown();
        reset();
        return;
      }

      EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

      new EntityFinder(selector).findAll(player).forEach(target -> {
        Attack attack = YamlReader.attack(config, player.getVelocity(), getDisplayName());
        AttackInfo attackInfo = new AttackInfo(AttackType.INCARNATION_SLAM, this);

        if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
          target.getWorld().playSound(target.getLocation(), Sound.SLIME_ATTACK, 2, 2);

          ItemStack stack = new ItemStack(Material.SLIME_BALL);
          Effects.itemBoom(SSL.getInstance(), target.getLocation(), stack, 4, 0.3, 10);
        }
      });
    }, 4, 0);
  }

  public void reset() {
    active = false;
    task.cancel();
    DisguiseAPI.undisguiseToAll(player);
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (active) {
      reset();
    }
  }
}
