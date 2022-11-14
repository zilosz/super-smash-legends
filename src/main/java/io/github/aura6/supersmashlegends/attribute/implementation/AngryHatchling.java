package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.LivingProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class AngryHatchling extends RightClickAbility {

    public AngryHatchling(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        HatchlingProjectile projectile = new HatchlingProjectile(plugin, this, config);
        projectile.setOverrideLocation(player.getLocation().setDirection(new Vector(0, -1, 0)));
        projectile.launch();
        player.setVelocity(new Vector(0, config.getDouble("Recoil"), 0));
    }

    public static class HatchlingProjectile extends LivingProjectile<Chicken> {

        public HatchlingProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public Chicken createEntity(Location location) {
            Chicken chicken = location.getWorld().spawn(location, Chicken.class);
            chicken.setAdult();
            return chicken;
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SNOW_SHOVEL).show(this.entity.getLocation());
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.CHICKEN_IDLE, 1, 1);
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.CHICKEN_HURT, 2, 1);
        }

        private void displayExplodeEffect(BlockFace face) {
            new ParticleBuilder(EnumParticle.SNOWBALL).setFace(face).solidSphere(this.entity.getLocation(), this.config.getDouble("Radius"), 65, 0.4);
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.FIREWORK_TWINKLE, 3, 1);
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 1, 1);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            displayExplodeEffect(null);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            displayExplodeEffect(result.getFace());

            EntityFinder finder = new EntityFinder(this.plugin, new DistanceSelector(this.config.getDouble("Radius")));

            finder.findAll(this.launcher, this.entity.getLocation()).forEach(target -> {
                Damage damage = getDamage();
                damage.setDirection(VectorUtils.fromTo(this.entity.getLocation(), target.getLocation()));
                AttributeDamageEvent event = new AttributeDamageEvent(target, damage, this.ability);

                if (this.plugin.getDamageManager().attemptAttributeDamage(event)) {
                    this.launcher.playSound(this.launcher.getLocation(), Sound.ORB_PICKUP, 2, 0.5f);
                };
            });
        }
    }
}
