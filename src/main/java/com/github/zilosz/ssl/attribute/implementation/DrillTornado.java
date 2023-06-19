package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;

public class DrillTornado extends ChargedRightClickAbility {

    public DrillTornado(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        player.setVelocity(player.getEyeLocation().getDirection().multiply(config.getDouble("Velocity")));
        player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 2, 1);

        for (double y = 0; y < 2 * Math.PI; y += config.getDouble("ParticleGap")) {
            Location particleLoc = player.getLocation().add(1.5 * Math.cos(y), y, 1.5 * Math.sin(y));
            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(particleLoc);
        }

        EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

        new EntityFinder(plugin, selector).findAll(player).forEach(target -> {
            AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection());

            if (plugin.getDamageManager().attack(target, this, settings)) {
                player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 0.5f);
            }
        });
    }

    @Override
    public void onGeneralCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 2, 1.5f);
    }
}
