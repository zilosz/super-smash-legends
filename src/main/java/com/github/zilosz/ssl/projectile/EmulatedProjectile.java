package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.block.BlockUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public abstract class EmulatedProjectile<T extends Entity> extends CustomProjectile<T> {
  protected boolean removeOnLongCollision;
  @Nullable private BlockFace lastHitFace;
  private int sameHitDuration;

  public EmulatedProjectile(Section config, AttackInfo attackInfo) {
    super(config, attackInfo);
    removeOnLongCollision = config.getOptionalBoolean("RemoveOnLongCollision").orElse(true);
  }

  @Override
  public void run() {
    super.run();

    double accuracy =
        SSL.getInstance().getResources().getConfig().getDouble("Collision.FaceAccuracy");
    BlockHitResult result = BlockUtils.findBlockHitByEntityBox(entity, accuracy);

    if (result == null) return;

    if (result.getFace() == null) {
      lastHitFace = null;
    }
    else if (result.getFace() == lastHitFace) {
      int maxStuckDuration =
          SSL.getInstance().getResources().getConfig().getInt("Collision.MaxStuckDuration");

      if (++sameHitDuration >= maxStuckDuration && removeOnLongCollision) {
        remove(ProjectileRemoveReason.HIT_BLOCK);
      }

    }
    else {
      sameHitDuration = 0;
      lastHitFace = result.getFace();
      hitBlock(result);
    }
  }
}
