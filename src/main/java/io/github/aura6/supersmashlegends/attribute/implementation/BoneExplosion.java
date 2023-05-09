package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.Effects;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BoneExplosion extends RightClickAbility {

    public BoneExplosion(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.SKELETON_HURT, 2, 1);

        double radius = config.getDouble("Radius");
        Effects.itemBoom(plugin, EntityUtils.center(player), new ItemStack(Material.BONE), radius, 1, 60);

        new EntityFinder(plugin, new DistanceSelector(radius)).findAll(player).forEach(target -> {
            double distanceSq = player.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.decLin(config, "Damage", distanceSq, radius * radius);
            double kb = YamlReader.decLin(config, "Kb", distanceSq, radius * radius);

            Vector direction = VectorUtils.fromTo(player, target);
            Damage dmg = Damage.Builder.fromConfig(config, direction).setDamage(damage).setKb(kb).build();

            plugin.getDamageManager().attemptAttributeDamage(target, dmg, this);
        });
    }
}
