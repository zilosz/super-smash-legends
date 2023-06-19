package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.kit.Kit;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class FrostedTwirl extends ChargedRightClickAbility {

    public FrostedTwirl(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        new ParticleBuilder(EnumParticle.SNOW_SHOVEL).ring(EntityUtils.center(player), 90, 0, 1, 20);
        player.getWorld().playSound(player.getLocation(), Sound.FIRE_IGNITE, 2, 1.5f);

        Vector forward = player.getEyeLocation().getDirection().multiply(config.getDouble("Velocity"));
        player.setVelocity(forward.setY(config.getDouble("VelocityY")));

        EntitySelector selector = new HitBoxSelector(config.getDouble("HitBox"));

        new EntityFinder(plugin, selector).findAll(player).forEach(target -> {
            AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection());

            if (plugin.getDamageManager().attack(target, this, settings)) {
                player.getWorld().playSound(player.getLocation(), Sound.GLASS, 2, 1);
            }
        });
    }
}
