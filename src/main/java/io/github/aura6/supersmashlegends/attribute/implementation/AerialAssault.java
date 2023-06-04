package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class AerialAssault extends ChargedRightClickAbility {
    private Vector velocity;

    public AerialAssault(SuperSmashLegends plugin, Section config, Kit kit) {
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

        new EntityFinder(this.plugin, new HitBoxSelector(this.config.getDouble("HitBox"))).findAll(this.player).forEach(target -> {
            Damage damage = Damage.Builder.fromConfig(this.config, this.velocity).build();

            if (this.plugin.getDamageManager().attemptAttributeDamage(target, damage, this)) {
                this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1, 1);
            }
        });
    }
}
