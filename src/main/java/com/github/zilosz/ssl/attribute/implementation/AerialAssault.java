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
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class AerialAssault extends ChargedRightClickAbility {
    private Vector velocity;

    public AerialAssault(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        double speed = this.config.getDouble("Speed");
        double y = this.config.getDouble("VelocityY");
        this.velocity = this.player.getEyeLocation().getDirection().multiply(speed).setY(y);
    }

    @Override
    public void onChargeTick() {
        if (EntityUtils.isPlayerGrounded(this.player)) return;

        this.player.setVelocity(this.velocity);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 2);

        Vector forward = this.velocity.clone().normalize().multiply(2);
        Location particleCenter = EntityUtils.center(this.player).setDirection(this.velocity).add(forward);
        new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).ring(particleCenter, 1.5, 20);

        EntitySelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

        new EntityFinder(this.plugin, selector).findAll(this.player).forEach(target -> {
            if (this.plugin.getDamageManager().attack(target, this, new AttackSettings(this.config, this.velocity))) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1, 1);
            }
        });
    }
}
