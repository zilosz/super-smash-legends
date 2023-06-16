package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.AttackSettings;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class ViceGrip extends RightClickAbility {

    public ViceGrip(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 2);

        EntityFinder finder = new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox")));

        Location location = player.getEyeLocation();
        Vector step = location.getDirection().multiply(0.25);

        boolean found = false;
        double stepped = 0;

        while (stepped <= config.getDouble("Range") && !location.getBlock().getType().isSolid() && !found) {

            for (LivingEntity target : finder.findAll(player, location)) {
                double kbY = step.clone().getY() + this.config.getDouble("ExtraY");

                AttackSettings settings = new AttackSettings(this.config, step)
                        .modifyKb(kbSettings -> kbSettings.setKbY(kbY));

                if (plugin.getDamageManager().attack(target, this, settings)) {
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    found = true;
                    break;
                }
            }

            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 0, 255).show(location);

            stepped += 0.25;
            location.add(step);
        }
    }
}
