package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ChargedRightClickBlockAbility;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.FloatingEntity;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.List;

public class GaiaToss extends ChargedRightClickBlockAbility {
    private final List<FloatingEntity<FallingBlock>> blocks = new ArrayList<>();
    private final List<BukkitTask> positionUpdaters = new ArrayList<>();
    private int currSize;
    private int increments = -1;
    private BukkitTask soundTask;
    private BukkitTask sizeTask;

    @Override
    public void onFailedCharge() {
        this.playSound();
        this.reset(true);
    }

    @Override
    public void onSuccessfulCharge() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 4, 0.5f);

        int minSize = this.config.getInt("MinSize");
        int val = this.currSize - minSize;
        int limit = this.getMaxSize() - minSize;

        double speed = YamlReader.increasingValue(this.config, "Speed", val, limit);
        double damage = YamlReader.increasingValue(this.config, "Damage", val, limit);
        double kb = YamlReader.increasingValue(this.config, "Kb", val, limit);

        this.launch(true, damage, kb, speed, this.blocks.get(0).getEntity());

        for (int i = 1; i < this.blocks.size(); i++) {
            this.launch(false, damage, kb, speed, this.blocks.get(i).getEntity());
        }

        this.reset(true);
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {

        this.soundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            double pitch = MathUtils.increasingValue(0.5, 2, this.getMaxChargeTicks(), this.ticksCharging);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.FIREWORK_LAUNCH, 1, (float) pitch);
        }, 0, 3);

        this.currSize = this.config.getInt("MinSize") - this.config.getInt("IncrementSize");

        Vector direction = this.player.getEyeLocation().getDirection().multiply(this.getMaxSize() / 2.0);
        Location centerInGround = event.getClickedBlock().getLocation().add(direction);

        this.sizeTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            if (++this.increments > this.config.getInt("IncrementCount")) return;

            this.currSize += this.config.getInt("IncrementSize");

            this.playSound();
            this.destroyBlocks();

            List<Location> locations = MathUtils.locationCube(centerInGround, this.currSize);
            double heightAboveHead = this.currSize / 2.0 + this.config.getDouble("HeightAboveHead");
            Location centerAboveHead = EntityUtils.top(this.player).add(new Vector(0, heightAboveHead, 0));
            Vector relative = VectorUtils.fromTo(locations.get(0), centerAboveHead);

            for (Location location : locations) {
                Block block = location.getBlock();
                Material type = block.getType();

                if (!type.isSolid()) {
                    type = Material.IRON_BLOCK;
                }

                Material finalType = type;

                FloatingEntity<FallingBlock> blockEntity = new FloatingEntity<>() {

                    @Override
                    public FallingBlock createEntity(Location location) {
                        return BlockUtils.spawnFallingBlock(location, finalType, block.getData());
                    }
                };

                Location carryLoc = location.add(relative);
                blockEntity.spawn(carryLoc);
                this.blocks.add(blockEntity);

                Vector relativeToLoc = VectorUtils.fromTo(this.player.getLocation(), carryLoc);

                this.positionUpdaters.add(Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
                    blockEntity.teleport(this.player.getLocation().add(relativeToLoc));
                }, 2, 2));
            }
        }, 0, (long) (this.getMaxChargeTicks() / (this.config.getDouble("IncrementCount") + 1)));
    }

    private int getMaxSize() {
        int minSize = this.config.getInt("MinSize");
        int incrementCount = this.config.getInt("IncrementCount");
        int incrementSize = this.config.getInt("IncrementSize");
        return minSize + incrementCount * incrementSize;
    }

    private void playSound() {
        for (FloatingEntity<FallingBlock> block : this.blocks) {
            block.getEntity().getWorld().playSound(block.getEntity().getLocation(), Sound.DIG_GRASS, 1, 1);
        }
    }

    private void destroyBlocks() {
        CollectionUtils.removeWhileIterating(this.blocks, FloatingEntity::destroy);
        CollectionUtils.removeWhileIterating(this.positionUpdaters, BukkitTask::cancel);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.increments != -1) {
            this.reset(false);
        }
    }

    private void launch(boolean particles, double damage, double kb, double speed, FallingBlock block) {
        GaiaTossProjectile projectile = new GaiaTossProjectile(this, this.config, particles);
        projectile.setMaterial(block.getMaterial());
        projectile.setData(block.getBlockData());
        projectile.setOverrideLocation(block.getLocation().setDirection(this.player.getEyeLocation().getDirection()));
        projectile.setSpeed(speed);
        projectile.getAttack().getDamage().setDamage(damage);
        projectile.getAttack().getKb().setKb(kb);
        projectile.launch();
    }

    private void reset(boolean cooldown) {
        this.increments = -1;

        this.soundTask.cancel();
        this.sizeTask.cancel();

        this.destroyBlocks();

        if (cooldown) {
            this.startCooldown();
        }
    }

    private static class GaiaTossProjectile extends BlockProjectile {
        private final boolean createParticles;

        public GaiaTossProjectile(Ability ability, Section config, boolean createParticles) {
            super(ability, config);
            this.createParticles = createParticles;
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 2, 1);
            new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(this.entity.getLocation());
        }

        @Override
        public void onTick() {
            if (this.createParticles) {
                for (int i = 0; i < 3; i++) {
                    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_NORMAL);
                    new ParticleMaker(particle).show(this.entity.getLocation());
                }
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            Location loc = this.entity.getLocation();
            this.entity.getWorld().playSound(loc, Sound.IRONGOLEM_DEATH, 2, 0.5f);

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
            new ParticleMaker(particle).boom(SSL.getInstance(), loc, 5, 0.5, 15);
        }
    }
}
