package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.Bow;
import com.github.zilosz.ssl.projectile.ArrowProjectile;
import com.github.zilosz.ssl.utils.RunnableUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.math.MathUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Barrage extends Bow {
    private int stage = 1;
    private BukkitTask stageTask;

    @Override
    public void onStart() {

        this.stageTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            if (this.stage > this.getStages()) return;

            this.player.setExp((float) this.stage / this.getStages());

            float pitch = (float) MathUtils.getIncreasingValue(0.5, 2, this.getStages(), this.stage - 1);
            this.player.playSound(this.player.getLocation(), Sound.ITEM_PICKUP, 2, pitch);

            if (this.stage == this.getStages()) {
                this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_PIG_DEATH, 3, 2);
            }

            this.stage++;
        }, this.config.getInt("StageTicks"), this.config.getInt("StageTicks"));
    }

    private int getStages() {
        return this.config.getInt("Stages");
    }

    @Override
    public void onShot(double force) {
        this.launch(force, true);
        int arrowCount = this.config.getInt("MaxArrowCount");
        int amount = (int) MathUtils.getIncreasingValue(1, arrowCount, this.getStages(), this.stage - 1);
        int interval = this.config.getInt("TicksBetweenShot");
        RunnableUtils.runTaskWithIntervals(SSL.getInstance(), amount - 1, interval, () -> this.launch(force, false));
    }

    private void launch(double force, boolean first) {
        BarrageArrow arrow = new BarrageArrow(this, this.config);
        arrow.setSpeed(force * this.config.getDouble("MaxSpeed"));

        if (first) {
            arrow.setSpread(0);
        }

        arrow.launch();
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

    private static class BarrageArrow extends ArrowProjectile {

        public BarrageArrow(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onLaunch() {
            this.launcher.getWorld().playSound(this.launcher.getLocation(), Sound.SHOOT_ARROW, 1, 2);
        }

        @Override
        public void onTick() {
            new ParticleMaker(new ParticleBuilder(ParticleEffect.SMOKE_NORMAL)).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(200, 200, 200));
            new ParticleMaker(particle).boom(SSL.getInstance(), this.entity.getLocation(), 1.2, 0.4, 6);
        }
    }
}
