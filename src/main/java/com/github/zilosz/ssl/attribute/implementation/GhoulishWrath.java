package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.utils.RunnableUtils;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.FloatingEntity;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class GhoulishWrath extends ChargedRightClickAbility {
    private FloatingEntity<FallingBlock> floatingBlock;
    private float pitch;

    @Override
    public void onChargeTick() {
        this.floatingBlock.teleport(this.getFloatingLocation());
        this.player.playSound(this.floatingBlock.getEntity().getLocation(), Sound.FIREWORK_LAUNCH, 1, this.pitch);
        this.pitch += 1.5 / this.getMaxChargeTicks();
    }

    @Override
    public void onFailedCharge() {
        this.player.getWorld().playSound(this.floatingBlock.getEntity().getLocation(), Sound.DIG_SAND, 2, 0.8f);
        this.reset();
    }

    @Override
    public void onSuccessfulCharge() {
        int count = this.config.getInt("BlockCount");
        int interval = this.config.getInt("LaunchInterval");
        int ticksCharged = this.ticksCharging;

        AttackInfo attackInfo = new AttackInfo(AttackType.GHOULISH_WRATH, this);
        double speed = YamlReader.increasingValue(this.config, "Velocity", ticksCharged, this.getMaxChargeTicks());

        RunnableUtils.runTaskWithIntervals(SSL.getInstance(), count, interval, () -> {
            this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_SHOOT, 0.5f, 2);

            SoulProjectile projectile = new SoulProjectile(this.config.getSection("Projectile"), attackInfo);
            projectile.setSpeed(speed);
            projectile.launch();
        });

        this.reset();
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        Material material = Material.valueOf(this.config.getString("Projectile.Block.Material"));
        this.pitch = 0.5f;

        this.floatingBlock = new FloatingEntity<>() {

            @Override
            public FallingBlock createEntity(Location location) {
                return BlockUtils.spawnFallingBlock(location, material);
            }
        };

        this.floatingBlock.spawn(this.getFloatingLocation());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    private void reset() {
        if (this.floatingBlock != null) {
            this.floatingBlock.destroy();
        }
    }

    private Location getFloatingLocation() {
        Location eyeLoc = this.player.getEyeLocation();
        return eyeLoc.add(eyeLoc.getDirection().multiply(this.config.getDouble("EyeDistance")));
    }

    private static class SoulProjectile extends BlockProjectile {

        public SoulProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.playEffect();

            for (int i = 0; i < 7; i++) {
                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SPELL_WITCH);
                new ParticleMaker(particle).setSpread(0.75f, 0.75f, 0.75f).show(this.entity.getLocation());
            }
        }

        @Override
        public void onTick() {
            new ParticleMaker(new ParticleBuilder(ParticleEffect.FIREWORKS_SPARK)).show(this.entity.getLocation());
        }

        @Override
        public void onPreTargetHit(LivingEntity target) {
            String attackPath = target.hasMetadata("pumpkin") ? "PumpkinAttack" : "NormalAttack";
            String name = ((GhoulishWrath) this.attackInfo.getAttribute()).getDisplayName();
            this.attack = YamlReader.attack(this.config.getSection(attackPath), null, name);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.playEffect();

            if (target.hasMetadata("pumpkin")) {
                new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(this.entity.getLocation());
                this.entity.getWorld().playSound(this.entity.getLocation(), Sound.WITHER_HURT, 1, 1);
            }
        }

        private void playEffect() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.DIG_GRAVEL, 3, 0.5f);
        }
    }
}
