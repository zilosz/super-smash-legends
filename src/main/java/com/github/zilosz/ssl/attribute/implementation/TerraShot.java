package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ChargedRightClickBlockAbility;
import com.github.zilosz.ssl.damage.DamageSettings;
import com.github.zilosz.ssl.damage.KbSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.BlockProjectile;
import com.github.zilosz.ssl.utils.RunnableUtils;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class TerraShot extends ChargedRightClickBlockAbility {
    private Item blockItem;
    private BukkitTask rotateTask;
    private float pitch;

    public TerraShot(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
        this.maxChargeTicks = config.getInt("StageDuration") * config.getInt("Stages");
    }

    @Override
    public void onInitialClick(PlayerInteractEvent event) {
        this.pitch = 0.5f;

        Block clickedBlock = event.getClickedBlock();
        ItemStack stack = new ItemStack(clickedBlock.getType(), 1, (short) 0, clickedBlock.getData());

        this.blockItem = this.player.getWorld()
                .dropItem(this.player.getLocation().add(0, this.config.getDouble("Height"), 0), stack);

        this.blockItem.setPickupDelay(Integer.MAX_VALUE);

        this.rotateTask = VectorUtils.rotateAroundEntity(
                this.plugin,
                this.blockItem,
                this.player,
                90,
                0,
                this.config.getDouble("Height"),
                this.config.getDouble("RotationRadius"),
                25
        );
    }

    @Override
    public void onChargeTick() {
        new ParticleBuilder(EnumParticle.REDSTONE).show(this.blockItem.getLocation());

        if (this.ticksCharging % this.config.getInt("StageDuration") == 0) {
            this.player.playSound(this.player.getLocation(), Sound.IRONGOLEM_HIT, 1, this.pitch);
            this.pitch += 1.5 / (this.config.getInt("Stages") - 1);
        }
    }

    @Override
    public void onFailedCharge() {
        this.stopRotation();
        this.player.playSound(this.player.getLocation(), Sound.DIG_GRASS, 2, 2);
    }

    @Override
    public void onSuccessfulCharge() {
        ItemStack stack = this.blockItem.getItemStack();
        Material material = stack.getType();
        byte data = stack.getData().getData();

        double speed = YamlReader.incLin(this.config, "Speed", this.ticksCharging, this.maxChargeTicks);
        double damage = YamlReader.incLin(this.config, "Damage", this.ticksCharging, this.maxChargeTicks);
        double kb = YamlReader.incLin(this.config, "Kb", this.ticksCharging, this.maxChargeTicks);

        int count = this.config.getInt("Count");
        int interval = this.config.getInt("LaunchInterval");

        RunnableUtils.runTaskWithIntervals(this.plugin, count, interval, () -> {
                    TerraProjectile projectile = new TerraProjectile(this.plugin, this, this.config);
                    projectile.setMaterial(material);
                    projectile.setData(data);

                    DamageSettings damageSettings = projectile.getAttackSettings().getDamageSettings();
                    damageSettings.setDamage(damage);

                    KbSettings kbSettings = projectile.getAttackSettings().getKbSettings();
                    kbSettings.setKb(kb);

                    projectile.setSpeed(speed);
                    projectile.launch();
                }, this::stopRotation
        );
    }

    private void stopRotation() {
        this.blockItem.remove();
        this.rotateTask.cancel();
    }

    public static class TerraProjectile extends BlockProjectile {

        public TerraProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.IRONGOLEM_DEATH, 1, 1);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.REDSTONE).setFace(result.getFace())
                    .setRgb(255, 0, 255)
                    .boom(this.plugin, this.entity.getLocation(), 4, 0.5, 15);
        }

        @Override
        public void onTick() {
            for (int i = 0; i < 5; i++) {
                new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 0, 255).show(this.entity.getLocation());
            }
        }

        @Override
        public void onTargetHit(LivingEntity victim) {
            new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, this.entity.getLocation(), 4, 0.5, 15);
        }
    }
}
