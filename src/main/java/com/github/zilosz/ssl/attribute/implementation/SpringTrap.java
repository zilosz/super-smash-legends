package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class SpringTrap extends RightClickAbility {
    private int uses = 0;

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpringProjectile(this, this.config).launch();

        double velocity = this.config.getDouble("ForwardVelocity");
        double velocityY = this.config.getDouble("ForwardVelocityY");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity).setY(velocityY));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_WOOD, 2, 2);

        if (++this.uses >= this.config.getInt("Uses")) {
            this.startCooldown();
            this.uses = 0;
        }
    }

    private static class SpringProjectile extends BlockProjectile {

        public SpringProjectile(Ability ability, Section config) {
            super(ability, config);
            this.overrideLocation = this.launcher.getLocation().setDirection(new Vector(0, -1, 0));
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.displayEffect();

            EntityFinder finder = new EntityFinder(new DistanceSelector(this.config.getDouble("Radius")));

            finder.findAll(this.launcher, this.entity.getLocation()).forEach(target -> {
                Vector direction = VectorUtils.fromTo(this.entity, target);
                Attack settings = YamlReader.attack(this.config.getSection("Aoe"), direction);
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
                new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).solidSphere(loc, radius, 5, 0.5);
            }

            this.entity.getWorld().playSound(loc, Sound.ZOMBIE_WOODBREAK, 2, 1);
        }
    }
}
