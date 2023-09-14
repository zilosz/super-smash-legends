package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class FrostedTwirl extends ChargedRightClickAbility {

    @Override
    public void onChargeTick() {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SNOW_SHOVEL);
        new ParticleMaker(particle).ring(EntityUtils.center(this.player), 90, 0, 1, 20);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIRE_IGNITE, 2, 1.5f);

        Vector forward = this.player.getEyeLocation().getDirection().multiply(this.config.getDouble("Velocity"));
        this.player.setVelocity(forward.setY(this.config.getDouble("VelocityY")));

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(selector).findAll(this.player).forEach(target -> {
            Attack attack = YamlReader.attack(this.config, this.player.getVelocity(), this.getDisplayName());
            AttackInfo attackInfo = new AttackInfo(AttackType.FROSTED_TWIRL, this);

            if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.GLASS, 2, 1);
            }
        });
    }
}
