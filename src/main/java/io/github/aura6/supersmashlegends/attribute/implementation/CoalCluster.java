package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.BlockProjectile;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;

public class CoalCluster extends RightClickAbility {

    public CoalCluster(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new ClusterProjectile(plugin, this, config.getSection("Cluster")).launch();
    }

    public static class ClusterProjectile extends BlockProjectile {

        public ClusterProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.breakIntoFragments();
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            this.breakIntoFragments();

            if (MathUtils.probability(config.getDouble("BurnChance"))) {
                victim.setFireTicks(config.getInt("BurnDuration"));
            }
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).show(this.entity.getLocation());
        }

        private void breakIntoFragments() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 1, 1);

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.entity.getLocation());
            }

            float yawStep = 360f / (config.getInt("FragmentCount") - 1);
            Location launchLoc = this.entity.getLocation().add(0, 0.5, 0);

            for (int i = 0; i < config.getInt("FragmentCount"); i++) {
                launchLoc.setPitch((float) MathUtils.randRange(config.getFloat("MinPitch"), config.getFloat("MaxPitch")));
                launchLoc.setYaw(i * yawStep);

                FragmentProjectile projectile = new FragmentProjectile(this.plugin, this.ability, config.getSection("Fragment"));
                projectile.setOverrideLocation(launchLoc);
                projectile.launch();
            }
        }
    }

    public static class FragmentProjectile extends ItemProjectile {

        public FragmentProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.FLAME).show(this.entity.getLocation());
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            if (MathUtils.probability(config.getDouble("BurnChance"))) {
                victim.setFireTicks(config.getInt("BurnDuration"));
            }
        }
    }
}
