package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;

public class CoalCluster extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new ClusterProjectile(this, this.config.getSection("Cluster")).launch();
    }

    private static class ClusterProjectile extends BlockProjectile {

        public ClusterProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.breakIntoFragments();
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            this.breakIntoFragments();

            if (MathUtils.probability(this.config.getDouble("BurnChance"))) {
                victim.setFireTicks(this.config.getInt("BurnDuration"));
            }
        }

        private void breakIntoFragments() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 2, 1);

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.entity.getLocation());
            }

            float yawStep = 360f / (this.config.getInt("FragmentCount") - 1);
            Location launchLoc = this.entity.getLocation().add(0, 0.5, 0);

            for (int i = 0; i < this.config.getInt("FragmentCount"); i++) {
                float minPitch = this.config.getFloat("MinPitch");
                float maxPitch = this.config.getFloat("MaxPitch");
                launchLoc.setPitch((float) MathUtils.randRange(minPitch, maxPitch));
                launchLoc.setYaw(i * yawStep);

                Section settings = this.config.getSection("Fragment");
                FragmentProjectile projectile = new FragmentProjectile(this.ability, settings);
                projectile.setOverrideLocation(launchLoc);
                projectile.launch();
            }
        }
    }

    private static class FragmentProjectile extends ItemProjectile {

        public FragmentProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).show(this.entity.getLocation());
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.FLAME).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            if (MathUtils.probability(this.config.getDouble("BurnChance"))) {
                victim.setFireTicks(this.config.getInt("BurnDuration"));
            }
        }
    }
}
