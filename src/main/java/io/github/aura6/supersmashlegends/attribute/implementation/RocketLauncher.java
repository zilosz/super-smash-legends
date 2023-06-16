package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.DamageSettings;
import io.github.aura6.supersmashlegends.damage.KbSettings;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.CollectionUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RocketLauncher extends ChargedRightClickAbility {

    private static final List<ParticleBuilder> PARTICLES = Arrays.asList(
            new ParticleBuilder(EnumParticle.REDSTONE),
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0),
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(0, 255, 0),
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(1, 1, 1),
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 140, 0),
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 165, 200)
    );

    private float pitch;

    public RocketLauncher(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        pitch = 0.5f;
    }

    @Override
    public void onChargeTick() {
        player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 0.5f, pitch += 1.5 / maxChargeTicks);
    }

    @Override
    public void onSuccessfulCharge() {
        Section main = config.getSection("Rocket");
        Rocket rocket = new Rocket(plugin, this, main);

        DamageSettings damageSettings = rocket.getAttackSettings().getDamageSettings();
        damageSettings.setDamage(YamlReader.incLin(main, "Damage", this.ticksCharging, this.maxChargeTicks));

        KbSettings kbSettings = rocket.getAttackSettings().getKbSettings();
        kbSettings.setKb(YamlReader.incLin(main, "Kb", this.ticksCharging, this.maxChargeTicks));

        rocket.setSpeed(YamlReader.incLin(main, "Speed", ticksCharging, maxChargeTicks));
        rocket.launch();
    }

    public static class Rocket extends ItemProjectile {
        private final List<Location> particles = new ArrayList<>();

        public Rocket(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
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

        private void launchShrapnel(Vector direction, LivingEntity toAvoid) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.FIREWORK_LAUNCH, 3, 1);

            Location location = this.entity.getLocation().add(0, 0.5, 0).setDirection(direction);
            float yaw = location.getYaw() - config.getFloat("Shrapnel.YawSpread") / 2;

            for (int i = 0; i < config.getInt("Shrapnel.Count"); i++) {
                Location launchLocation = location.clone();
                launchLocation.setYaw(yaw);

                ParticleBuilder particle = CollectionUtils.selectRandom(PARTICLES);
                Shrapnel shrapnel = new Shrapnel(this.plugin, this.ability, config.getSection("Shrapnel"), particle, toAvoid);

                double multiplier =  config.getDouble("Shrapnel.Multiplier");

                DamageSettings damageSettings = shrapnel.getAttackSettings().getDamageSettings();
                damageSettings.setDamage(damageSettings.getDamage() * multiplier);

                KbSettings kbSettings = shrapnel.getAttackSettings().getKbSettings();
                kbSettings.setKb(kbSettings.getKb() * multiplier);

                shrapnel.setOverrideLocation(launchLocation);
                shrapnel.setSpeed(this.launchSpeed * multiplier);

                shrapnel.launch();

                yaw += config.getFloat("Shrapnel.YawSpread") / config.getInt("Shrapnel.Count");
            }
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 3, 1);

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).show(this.entity.getLocation());
            }

            if (result.getFace() == BlockFace.UP || result.getFace() == BlockFace.DOWN) {
                Location location = this.entity.getLocation().setDirection(this.entity.getVelocity());
                float pitch = config.getFloat("Shrapnel.Pitch");
                location.setPitch(result.getFace() == BlockFace.UP ? -pitch : pitch);
                launchShrapnel(location.getDirection(), null);
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            launchShrapnel(this.entity.getVelocity(), target);
        }
    }

    public static class Shrapnel extends ItemProjectile {
        private final ParticleBuilder particle;
        private final LivingEntity toAvoid;

        public Shrapnel(SuperSmashLegends plugin, Ability ability, Section config, ParticleBuilder particle, LivingEntity toAvoid) {
            super(plugin, ability, config);
            this.particle = particle;
            this.toAvoid = toAvoid;
        }

        @Override
        public EntityFinder getFinder() {
            EntityFinder finder = super.getFinder();

            if (toAvoid != null) {
                finder.avoid(toAvoid);
            }

            return finder;
        }

        @Override
        public void onTick() {
            for (int i = 0; i < 3; i++) {
                this.particle.setSpread(0.2f, 0.2f, 0.2f).show(this.entity.getLocation());
            }
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 1, 1);
        }
    }
}
