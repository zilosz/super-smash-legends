package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Bow;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.RunnableUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class Barrage extends Bow {
    private int stage;
    private float pitch;

    public Barrage(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onStart() {
        stage = 1;
        pitch = 0.5f;
    }

    @Override
    public void onChargeTick() {
        if (stage >= config.getInt("MaxStage")) return;

        if (ticksCharging % config.getInt("StageDuration") == 0) {
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 2, pitch);
            pitch += 1.5 / config.getInt("MaxStage");

            if (++stage == config.getInt("MaxStage")) {
                player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_DEATH, 3, 2);
            }
        }
    }

    private void launch(double force, boolean first) {
        BarrageArrow arrow = new BarrageArrow(plugin, this, config.getSection("Projectile"));
        arrow.setSpeed(force * config.getDouble("MaxSpeed"));
        if (first) arrow.setSpread(0);
        arrow.launch();
    }

    @Override
    public void onShot(double force) {
        launch(force, true);
        int amount = stage * config.getInt("ArrowsPerStage") - 1;
        RunnableUtils.runTaskWithIntervals(plugin, amount, config.getInt("ShotInterval"), () -> launch(force, false));
    }

    public static class BarrageArrow extends ItemProjectile {

        public BarrageArrow(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            this.launcher.getWorld().playSound(this.launcher.getLocation(), Sound.SKELETON_HURT, 1, 2);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SMOKE_NORMAL).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(200, 200, 200)
                    .boom(this.plugin, this.entity.getLocation(), 1.5, 0.25, 12);
        }
    }
}
