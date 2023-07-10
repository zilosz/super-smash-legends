package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.utils.effects.Effects;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BoneExplosion extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SKELETON_HURT, 2, 1);

        double radius = this.config.getDouble("Radius");
        Location center = EntityUtils.center(this.player);
        Effects.itemBoom(SSL.getInstance(), center, new ItemStack(Material.BONE), radius, 0.8, 60);

        new EntityFinder(new DistanceSelector(radius)).findAll(this.player).forEach(target -> {
            double distanceSq = this.player.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.getDecreasingValue(this.config, "Damage", distanceSq, radius * radius);
            double kb = YamlReader.getDecreasingValue(this.config, "Kb", distanceSq, radius * radius);

            Vector direction = VectorUtils.fromTo(this.player, target);

            Attack attack = new Attack(this.config, direction);
            attack.getDamage().setDamage(damage);
            attack.getKb().setKb(kb);

            SSL.getInstance().getDamageManager().attack(target, this, attack);
        });
    }
}
