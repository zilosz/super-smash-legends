package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class SpringTrap extends RightClickAbility {
    private int uses = 0;

    public SpringTrap(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpringProjectile(this.plugin, this, this.config).launch();

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

        private void displayEffect() {
            for (int i = 0; i < 2; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).solidSphere(this.entity.getLocation(), config.getDouble("Radius"), 5, 0.5);
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            displayEffect();
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            displayEffect();

            EntityFinder finder = new EntityFinder(this.plugin, new DistanceSelector(config.getDouble("Radius")));

            finder.findAll(this.launcher, this.entity.getLocation()).forEach(target -> {
                Vector direction = VectorUtils.fromTo(this.entity, target);
                AttackSettings settings = new AttackSettings(this.config.getSection("Aoe"), direction);
                this.plugin.getDamageManager().attack(target, this.ability, settings);
            });
        }
    }
}
