package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.effects.Effects;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BoneExplosion extends RightClickAbility {

  @Override
  public void onClick(PlayerInteractEvent event) {
    player.getWorld().playSound(player.getLocation(), Sound.SKELETON_HURT, 2, 1);

    double radius = config.getDouble("Radius");
    Location center = EntityUtils.center(player);
    Effects.itemBoom(SSL.getInstance(), center, new ItemStack(Material.BONE), radius, 0.8, 60);

    new EntityFinder(new DistanceSelector(radius)).findAll(player).forEach(target -> {
      double distanceSq = player.getLocation().distanceSquared(target.getLocation());
      double damage = YamlReader.decreasingValue(config, "Damage", distanceSq, radius * radius);
      double kb = YamlReader.decreasingValue(config, "Kb", distanceSq, radius * radius);

      Vector direction = VectorUtils.fromTo(player, target);

      Attack attack = YamlReader.attack(config, direction, getDisplayName());
      attack.getDamage().setDamage(damage);
      attack.getKb().setKb(kb);

      AttackInfo attackInfo = new AttackInfo(AttackType.BONE_EXPLOSION, this);
      SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
    });
  }
}
