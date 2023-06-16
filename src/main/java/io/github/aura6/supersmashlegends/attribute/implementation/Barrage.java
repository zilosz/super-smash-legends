package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Bow;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ArrowProjectile;
import io.github.aura6.supersmashlegends.utils.RunnableUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

public class Barrage extends Bow {
    private int stage = 1;
    private BukkitTask stageTask;

    public Barrage(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private int getStages() {
        return this.config.getInt("Stages");
    }

    @Override
    public void onStart() {

        this.stageTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (this.stage > this.getStages()) return;

            this.player.setExp((float) this.stage / this.getStages());

            float pitch = (float) MathUtils.increasingLinear(0.5, 2, this.config.getInt("Stages"), this.stage - 1);
            this.player.playSound(this.player.getLocation(), Sound.ITEM_PICKUP, 2, pitch);

            if (this.stage == this.getStages()) {
                this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_PIG_DEATH, 3, 2);
            }

            this.stage++;
        }, this.config.getInt("StageTicks"), this.config.getInt("StageTicks"));
    }

    private void launch(double force, boolean first) {
        BarrageArrow arrow = new BarrageArrow(this.plugin, this, this.config.getSection("Projectile"));
        arrow.setSpeed(force * this.config.getDouble("MaxSpeed"));

        if (first) {
            arrow.setSpread(0);
        }

        arrow.launch();
    }

    @Override
    public void onShot(double force) {
        this.launch(force, true);
        int arrowCount = this.config.getInt("MaxArrowCount");
        int amount = (int) MathUtils.increasingLinear(1, arrowCount, this.getStages(), this.stage - 1);
        int interval = this.config.getInt("TicksBetweenShot");
        RunnableUtils.runTaskWithIntervals(this.plugin, amount - 1, interval, () -> this.launch(force, false));
    }

    @Override
    public void onFinish() {
        this.stage = 1;
        this.player.setExp(0);

        if (this.stageTask != null) {
            this.stageTask.cancel();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.onFinish();
    }

    public static class BarrageArrow extends ArrowProjectile {

        public BarrageArrow(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            this.launcher.getWorld().playSound(this.launcher.getLocation(), Sound.SHOOT_ARROW, 1, 2);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SMOKE_NORMAL).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            new ParticleBuilder(EnumParticle.REDSTONE)
                    .setRgb(200, 200, 200)
                    .boom(this.plugin, this.entity.getLocation(), 1.2, 0.4, 6);
        }
    }
}
