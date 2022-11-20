package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickBlockAbility;
import io.github.aura6.supersmashlegends.event.JumpEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.BlockProjectile;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BlockToss extends ChargedRightClickBlockAbility {
    private List<FallingBlock> carriedBlocks;
    private List<BukkitTask> carryTasks;
    private Location groundCenter;
    private int size;
    private float pitch;

    public BlockToss(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    public int getMaxSize() {
        return config.getInt("MinSize") + config.getInt("Increments") * config.getInt("SizeIncrement");
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        pitch = 0.5f;
        size = config.getInt("MinSize");

        carryTasks = new ArrayList<>();

        groundCenter = event.getClickedBlock().getLocation().subtract(0, 0.5 * getMaxSize(), 0);
        carriedBlocks = displayBlocks();
    }

    @Override
    public void onChargeTick() {
        player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, pitch += 1.5 / maxChargeTicks);

        if (ticksCharging % 5 == 0) {
            new ParticleBuilder(EnumParticle.ENCHANTMENT_TABLE).ring(player.getEyeLocation(), 90, 0, 1, 30);
        }

        if (ticksCharging % (maxChargeTicks / (config.getInt("Increments") + 1)) == 0 && size < getMaxSize()) {
            size++;
            stopCarry();
            carriedBlocks = displayBlocks();
        }
    }

    private void launch(boolean particles, double damage, double kb, double speed, FallingBlock block) {
        BlockTossProjectile projectile = new BlockTossProjectile(plugin, this, config, particles);
        projectile.setMaterial(block.getMaterial());
        projectile.setData(block.getBlockData());
        projectile.setOverrideLocation(block.getLocation().setDirection(player.getEyeLocation().getDirection()));
        projectile.setOverrideSpeed(speed);
        projectile.getDamage().setDamage(damage);
        projectile.getDamage().setKb(kb);
        projectile.launch();
    }

    @Override
    public void onSuccessfulCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 4, 0.5f);

        int limit = getMaxSize() - config.getInt("MinSize");
        double speed = YamlReader.incLin(config, "Speed", size - 1, limit);
        double damage = YamlReader.incLin(config, "Damage", size - 1, limit);
        double kb = YamlReader.incLin(config, "Kb", size - 1, limit);

        launch(true, damage, kb, speed, carriedBlocks.get(0));

        for (int i = 1; i < carriedBlocks.size(); i++) {
            launch(false, damage, kb, speed, carriedBlocks.get(i));
        }
    }

    @Override
    public void onFailedCharge() {
        carriedBlocks.forEach(block -> player.getWorld().playSound(block.getLocation(), Sound.DIG_GRASS, 1, 1));
    }

    @Override
    public void onGeneralCharge() {
        stopCarry();
    }

    private void stopCarry() {
        carryTasks.forEach(BukkitTask::cancel);
        carriedBlocks.forEach(FallingBlock::remove);
    }

    private List<FallingBlock> displayBlocks() {
        double height = EntityUtils.height(player) + config.getDouble("GapAbovePlayer") + 0.5 * size;
        Location centerAbovePlayer = player.getLocation().add(0, height, 0);

        List<Location> cubeLocations = MathUtils.getLocationCube(groundCenter.clone(), size);
        Location relative = centerAbovePlayer.subtract(cubeLocations.get(0));

        List<FallingBlock> fallingBlocks = new ArrayList<>();

        cubeLocations.forEach(location -> {
            player.getWorld().playSound(location, Sound.DIG_GRASS, 1, 1);

            Block block = location.getBlock();
            Material material = block.getType().isSolid() ? block.getType() : Material.IRON_BLOCK;

            Location initialLoc = location.add(relative);

            FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(initialLoc, material, block.getData());
            fallingBlock.setHurtEntities(false);
            fallingBlock.setDropItem(false);
            fallingBlocks.add(fallingBlock);

            Location offset = initialLoc.subtract(player.getLocation());

            carryTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                fallingBlock.setVelocity(player.getVelocity().add(new Vector(0, 0.105, 0)));
                fallingBlock.teleport((player.getLocation().add(offset)));
            }, 0, 0));
        });

        return fallingBlocks;
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock && carriedBlocks != null && carriedBlocks.contains((FallingBlock) event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (event.getPlayer() == player && ticksCharging > 0) {
            event.setHeight(event.getHeight() * config.getDouble("JumpMultiplier"));
            event.setPower(event.getPower() * config.getDouble("JumpMultiplier"));
        }
    }

    public static class BlockTossProjectile extends BlockProjectile {
        private final boolean createParticles;

        public BlockTossProjectile(SuperSmashLegends plugin, Ability ability, Section config, boolean createParticles) {
            super(plugin, ability, config);
            this.createParticles = createParticles;
        }

        @Override
        public void onTick() {
            if (createParticles) {
                for (int i = 0; i < 3; i++) {
                    new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).show(this.entity.getLocation());
                }
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.IRONGOLEM_DEATH, 4, 0.5f);
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).boom(this.plugin, this.entity.getLocation(), 5, 0.5, 15);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 3, 1);
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.entity.getLocation());
        }
    }
}
