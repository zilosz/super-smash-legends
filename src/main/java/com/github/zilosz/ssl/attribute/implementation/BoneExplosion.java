package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.Effects;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BoneExplosion extends RightClickAbility {

    public BoneExplosion(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.SKELETON_HURT, 2, 1);

        double radius = config.getDouble("Radius");
        Effects.itemBoom(plugin, EntityUtils.center(player), new ItemStack(Material.BONE), radius, 0.8, 60);

        new EntityFinder(plugin, new DistanceSelector(radius)).findAll(player).forEach(target -> {
            double distanceSq = player.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.decLin(config, "Damage", distanceSq, radius * radius);
            double kb = YamlReader.decLin(config, "Kb", distanceSq, radius * radius);

            Vector direction = VectorUtils.fromTo(player, target);

            plugin.getDamageManager().attack(target, this, new AttackSettings(this.config, direction)
                    .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                    .modifyKb(kbSettings -> kbSettings.setKb(kb)));
        });
    }
}
