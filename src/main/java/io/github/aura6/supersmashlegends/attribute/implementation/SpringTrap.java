package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.BlockProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class SpringTrap extends RightClickAbility {
    private int uses = 0;

    public SpringTrap(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new SpringProjectile(plugin, this, config).launch();
        player.setVelocity(new Vector(0, config.getDouble("Recoil"), 0));

        if (++uses >= config.getInt("Uses")) {
            startCooldown();
            uses = 0;
        }
    }

    public static class SpringProjectile extends BlockProjectile {

        public SpringProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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
                Damage damage = Damage.Builder.fromConfig(config.getSection("Aoe"), VectorUtils.fromTo(this.entity, target)).build();
                this.plugin.getDamageManager().attemptAttributeDamage(target, damage, this.ability);
            });
        }
    }
}
