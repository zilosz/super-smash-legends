package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.utils.NmsUtils;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ArrowProjectile extends ActualProjectile<Arrow> {

    public ArrowProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        super(plugin, ability, config);
    }

    @Override
    public Arrow createProjectile(Location location) {
        return location.getWorld().spawn(location, Arrow.class);
    }

    @Override
    public void onTargetHit(LivingEntity target) {
        super.onTargetHit(target);

        if (target instanceof Player) {
            NmsUtils.getPlayer((Player) target).getDataWatcher().watch(9, (byte) 0);
        }
    }
}
