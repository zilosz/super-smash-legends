package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.DamageSettings;
import com.github.zilosz.ssl.damage.KbSettings;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.CollectionUtils;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ColorType;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

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
        Rocket rocket = new Rocket(SSL.getInstance(), this, main);

        DamageSettings damageSettings = rocket.getAttackSettings().getDamageSettings();
        damageSettings.setDamage(YamlReader.incLin(main, "Damage", this.ticksCharging, this.maxChargeTicks));

        KbSettings kbSettings = rocket.getAttackSettings().getKbSettings();
        kbSettings.setKb(YamlReader.incLin(main, "Kb", this.ticksCharging, this.maxChargeTicks));

        rocket.setSpeed(YamlReader.incLin(main, "Speed", this.ticksCharging, this.maxChargeTicks));
        rocket.launch();
    }

    public static class Rocket extends ItemProjectile {
        private final List<Location> particles = new ArrayList<>();

        public Rocket(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 3, 1);

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).show(this.entity.getLocation());
            }

            if (result.getFace() == BlockFace.UP || result.getFace() == BlockFace.DOWN) {
                Location location = this.entity.getLocation().setDirection(this.entity.getVelocity());
                float pitch = this.config.getFloat("Shrapnel.Pitch");
                location.setPitch(result.getFace() == BlockFace.UP ? -pitch : pitch);
                this.launchShrapnel(location.getDirection(), null);
            }
        }

        @Override
        public void onTick() {
            Location location = this.entity.getLocation();
            this.entity.getWorld().playSound(location, Sound.FUSE, 0.5f, 1);

            this.particles.add(location);

            for (Location loc : this.particles) {
                new ParticleBuilder(EnumParticle.SMOKE_LARGE).setSpread(0.3f, 0.3f, 0.3f).show(loc);
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

                Color colorType = CollectionUtils.selectRandom(ColorType.values()).getColor();

                ParticleBuilder particle = new ParticleBuilder(EnumParticle.REDSTONE).setRgb(
                        colorType.getRed(),
                        colorType.getGreen(),
                        colorType.getBlue()
                );

                Section shrapnelConfig = this.config.getSection("Shrapnel");
                Shrapnel shrapnel = new Shrapnel(SSL.getInstance(), this.ability, shrapnelConfig, particle, toAvoid);

                double multiplier = this.config.getDouble("Shrapnel.Multiplier");

                DamageSettings damageSettings = shrapnel.getAttackSettings().getDamageSettings();
                damageSettings.setDamage(damageSettings.getDamage() * multiplier);

                KbSettings kbSettings = shrapnel.getAttackSettings().getKbSettings();
                kbSettings.setKb(kbSettings.getKb() * multiplier);

                shrapnel.setOverrideLocation(launchLocation);
                shrapnel.setSpeed(this.launchSpeed * multiplier);

                shrapnel.launch();

                yaw += this.config.getFloat("Shrapnel.YawSpread") / this.config.getInt("Shrapnel.Count");
            }
        }
    }

    public static class Shrapnel extends ItemProjectile {
        private final ParticleBuilder particle;
        private final LivingEntity toAvoid;

        public Shrapnel(SSL plugin, Ability ability, Section config, ParticleBuilder particle, LivingEntity toAvoid) {
            super(plugin, ability, config);
            this.particle = particle;
            this.toAvoid = toAvoid;
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 1, 1);
        }

        @Override
        public void onTick() {
            for (int i = 0; i < 3; i++) {
                this.particle.setSpread(0.2f, 0.2f, 0.2f).show(this.entity.getLocation());
            }
        }

        @Override
        public EntityFinder getFinder() {
            EntityFinder finder = super.getFinder();

            if (this.toAvoid != null) {
                finder.avoid(this.toAvoid);
            }

            return finder;
        }
    }
}
