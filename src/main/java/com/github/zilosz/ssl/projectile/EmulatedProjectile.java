package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public abstract class EmulatedProjectile<T extends Entity> extends CustomProjectile<T> {
    protected boolean removeOnLongCollision;
    @Nullable private BlockFace lastHitFace;
    private int sameHitDuration = 0;

    public EmulatedProjectile(Ability ability, Section config) {
        super(ability, config);
        this.removeOnLongCollision = config.getOptionalBoolean("RemoveOnLongCollision").orElse(true);
    }

    @Override
    public void run() {
        super.run();

        double accuracy = SSL.getInstance().getResources().getConfig().getDouble("Collision.FaceAccuracy");
        BlockHitResult result = BlockUtils.findBlockHitByEntityBox(this.entity, accuracy);

        if (result == null) return;

        if (result.getFace() == null) {
            this.lastHitFace = null;

        } else if (result.getFace() == this.lastHitFace) {
            int maxStuckDuration = SSL.getInstance().getResources().getConfig().getInt("Collision.MaxStuckDuration");

            if (++this.sameHitDuration >= maxStuckDuration && this.removeOnLongCollision) {
                this.remove(ProjectileRemoveReason.HIT_BLOCK);
            }

        } else {
            this.sameHitDuration = 0;
            this.lastHitFace = result.getFace();
            this.hitBlock(result);
        }
    }
}
