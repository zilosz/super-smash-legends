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

public class SlimyRetreat extends RightClickAbility {

    public SlimyRetreat(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.SLIME_ATTACK, 3, 0.5f);
        Effects.itemBoom(plugin, EntityUtils.center(player), new ItemStack(Material.SLIME_BALL), 5, 0.3, 20);

        player.setVelocity(player.getEyeLocation().getDirection().multiply(-config.getDouble("Velocity")));

        double radius = config.getDouble("Radius");

        new EntityFinder(plugin, new DistanceSelector(radius)).findAll(player).forEach(target -> {
            double distance = target.getLocation().distanceSquared(player.getLocation());
            double dmg = YamlReader.decLin(config, "Damage", distance, radius * radius);
            double kb = YamlReader.decLin(config, "Kb", distance, radius * radius);

            Vector direction = VectorUtils.fromTo(player, target);
            Damage damage = Damage.Builder.fromConfig(config, direction).setDamage(dmg).setKb(kb).build();

            plugin.getDamageManager().attemptAttributeDamage(target, damage, this);

            player.getWorld().playSound(target.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
        });
    }
}
