package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.block.BlockUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public abstract class EmulatedProjectile<T extends Entity> extends CustomProjectile<T> {
    @Getter @Setter protected boolean removeOnLongCollision;
    private BlockFace lastHitFace;
    private int sameHitDuration = 0;

    public EmulatedProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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
