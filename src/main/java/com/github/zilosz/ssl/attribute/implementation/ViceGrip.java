package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class ViceGrip extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.playSound(this.player.getLocation(), Sound.ENDERMAN_TELEPORT, 3, 2);

        EntityFinder finder = new EntityFinder(new HitBoxSelector(this.config.getDouble("HitBox")));

        Location location = this.player.getEyeLocation();
        Vector step = location.getDirection().multiply(0.25);

        boolean found = false;
        double stepped = 0;

        while (stepped <= this.config.getDouble("Range") && !location.getBlock().getType().isSolid() && !found) {

            for (LivingEntity target : finder.findAll(this.player, location)) {
                Attack attack = new Attack(this.config, step);
                attack.getKb().setKbY(step.clone().getY() + this.config.getDouble("ExtraY"));

                if (SSL.getInstance().getDamageManager().attack(target, this, attack)) {
                    this.player.playSound(this.player.getLocation(), Sound.ORB_PICKUP, 1, 1);
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
