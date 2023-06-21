package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class FrostedTwirl extends ChargedRightClickAbility {

    public FrostedTwirl(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        new ParticleBuilder(EnumParticle.SNOW_SHOVEL).ring(EntityUtils.center(this.player), 90, 0, 1, 20);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIRE_IGNITE, 2, 1.5f);

        Vector forward = this.player.getEyeLocation().getDirection().multiply(this.config.getDouble("Velocity"));
        this.player.setVelocity(forward.setY(this.config.getDouble("VelocityY")));

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(this.plugin, selector).findAll(this.player).forEach(target -> {
            AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection());

            if (this.plugin.getDamageManager().attack(target, this, settings)) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.GLASS, 2, 1);
            }
        });
    }
}
