package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class SpringTrap extends RightClickAbility {
    private int uses = 0;

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpringProjectile(SSL.getInstance(), this, this.config).launch();

        double velocity = this.config.getDouble("ForwardVelocity");
        double velocityY = this.config.getDouble("ForwardVelocityY");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity).setY(velocityY));

        if (++this.uses >= this.config.getInt("Uses")) {
            this.startCooldown();
            this.uses = 0;
        }
    }

    public static class SpringProjectile extends BlockProjectile {

        public SpringProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
            this.overrideLocation = this.launcher.getLocation().setDirection(new Vector(0, -1, 0));
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.displayEffect();

            EntityFinder finder = new EntityFinder(SSL.getInstance(), new DistanceSelector(this.config.getDouble("Radius")));

            finder.findAll(this.launcher, this.entity.getLocation()).forEach(target -> {
                Vector direction = VectorUtils.fromTo(this.entity, target);
                AttackSettings settings = new AttackSettings(this.config.getSection("Aoe"), direction);
                SSL.getInstance().getDamageManager().attack(target, this.ability, settings);
            });
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.displayEffect();
        }

        private void displayEffect() {
            Location loc = this.entity.getLocation();
            double radius = this.config.getDouble("Radius");

            for (int i = 0; i < 2; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).solidSphere(loc, radius, 5, 0.5);
            }
        }
    }
}
