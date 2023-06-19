package com.github.zilosz.ssl.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public abstract class EmulatedProjectile<T extends Entity> extends CustomProjectile<T> {
    @Getter @Setter protected boolean removeOnLongCollision;
    private BlockFace lastHitFace;
    private int sameHitDuration = 0;

    public EmulatedProjectile(SSL plugin, Ability ability, Section config) {
        super(plugin, ability, config);
        removeOnLongCollision = config.getOptionalBoolean("RemoveOnLongCollision").orElse(true);
    }

    @Override
    public void run() {
        super.run();

        double accuracy = plugin.getResources().getConfig().getDouble("Collision.FaceAccuracy");
        BlockHitResult result = BlockUtils.findBlockHitByEntityBox(entity, accuracy);

        if (result == null) return;

        if (result.getFace() == null) {
            lastHitFace = null;

        } else if (result.getFace() == lastHitFace) {
            int maxStuckDuration = plugin.getResources().getConfig().getInt("Collision.MaxStuckDuration");

            if (++sameHitDuration >= maxStuckDuration && removeOnLongCollision) {
                remove(ProjectileRemoveReason.HIT_BLOCK);
            }

        } else {
            sameHitDuration = 0;
            lastHitFace = result.getFace();
            handleBlockHitResult(result);
        }
    }
}
