package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.util.NmsUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ArrowProjectile extends ActualProjectile<Arrow> {

    public ArrowProjectile(Section config, AttackInfo attackInfo) {
        super(config, attackInfo);
    }

    @Override
    public Arrow createProjectile(Location location) {
        return location.getWorld().spawn(location, Arrow.class);
    }

    @Override
    public void onTargetHit(LivingEntity target) {
        if (target instanceof Player) {
            NmsUtils.getPlayer((Player) target).getDataWatcher().watch(9, (byte) 0);
        }
    }
}
