package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.Damage;
import com.github.zilosz.ssl.damage.KnockBack;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.effects.ColorType;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class RocketLauncher extends ChargedRightClickAbility {
    private float pitch;

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        this.pitch = 0.5f;
    }

    @Override
    public void onChargeTick() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 0.5f, this.pitch);
        this.pitch += 1.5 / this.maxChargeTicks;
    }

    @Override
    public void onSuccessfulCharge() {
        Section main = this.config.getSection("Rocket");
        Rocket rocket = new Rocket(this, main);

        Damage damage = rocket.getAttack().getDamage();
        damage.setDamage(YamlReader.increasingValue(main, "Damage", this.ticksCharging, this.maxChargeTicks));

        KnockBack kb = rocket.getAttack().getKb();
        kb.setKb(YamlReader.increasingValue(main, "Kb", this.ticksCharging, this.maxChargeTicks));

        rocket.setSpeed(YamlReader.increasingValue(main, "Speed", this.ticksCharging, this.maxChargeTicks));
        rocket.launch();

        this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 2, 1);
    }

    private static class Rocket extends ItemProjectile {

        public Rocket(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            Location loc = this.entity.getLocation();
            this.entity.getWorld().playSound(loc, Sound.EXPLODE, 3, 1);

            new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(loc);

            if (result.getFace() == BlockFace.UP || result.getFace() == BlockFace.DOWN) {
                Location location = loc.setDirection(this.entity.getVelocity());
                float pitch = this.config.getFloat("Shrapnel.Pitch");
                location.setPitch(result.getFace() == BlockFace.UP ? -pitch : pitch);
                this.launchShrapnel(location.getDirection(), null);
            }
        }

        @Override
        public void onTick() {
            Location location = this.entity.getLocation();
            this.entity.getWorld().playSound(location, Sound.FUSE, 0.5f, 1);

            for (int i = 0; i < 3; i++) {
                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
                new ParticleMaker(particle).setSpread(0.2).show(location);
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.launchShrapnel(this.entity.getVelocity(), target);
        }

        private void launchShrapnel(Vector direction, LivingEntity toAvoid) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.FIREWORK_LAUNCH, 3, 1);

            Location location = this.entity.getLocation().add(0, 0.5, 0).setDirection(direction);
            float yaw = location.getYaw() - this.config.getFloat("Shrapnel.YawSpread") / 2;

            for (int i = 0; i < this.config.getInt("Shrapnel.Count"); i++) {
                Location launchLocation = location.clone();
                launchLocation.setYaw(yaw);

                Color color = CollectionUtils.selectRandom(ColorType.values()).getParticleColor();
                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(color);

                Section shrapnelConfig = this.config.getSection("Shrapnel");
                Shrapnel shrapnel = new Shrapnel(this.ability, shrapnelConfig, particle, toAvoid);

                double multiplier = this.config.getDouble("Shrapnel.Multiplier");

                Damage damageSettings = shrapnel.getAttack().getDamage();
                damageSettings.setDamage(damageSettings.getDamage() * multiplier);

                KnockBack kbSettings = shrapnel.getAttack().getKb();
                kbSettings.setKb(kbSettings.getKb() * multiplier);

                shrapnel.setOverrideLocation(launchLocation);
                shrapnel.setSpeed(this.launchSpeed * multiplier);

                shrapnel.launch();

                yaw += this.config.getFloat("Shrapnel.YawSpread") / this.config.getInt("Shrapnel.Count");
            }
        }
    }

    private static class Shrapnel extends ItemProjectile {
        private final ParticleBuilder particle;

        public Shrapnel(Ability ability, Section config, ParticleBuilder particle, LivingEntity toAvoid) {
            super(ability, config);
            this.particle = particle;

            if (toAvoid != null) {
                this.entityFinder.avoid(toAvoid);
            }
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 1, 1);
        }

        @Override
        public void onTick() {
            for (int i = 0; i < 3; i++) {
                new ParticleMaker(this.particle).setSpread(0.2).show(this.entity.getLocation());
            }
        }
    }
}
