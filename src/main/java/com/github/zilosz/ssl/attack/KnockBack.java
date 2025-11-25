package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.util.math.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class KnockBack {
  private Vector direction;
  private double kb;
  private double kbY;
  private boolean factorsKit;
  private boolean factorsHealth;
  private boolean isLinear;
  private boolean factorsPreviousVelocity;
  private double minHealthBoost;
  private double maxHealthBoost;

  public Optional<Vector> getFinalKbVector(Damageable victim) {
    if (direction == null) return Optional.empty();

    double finalKb = getFinalKb(victim);
    Vector mul = new Vector(finalKb, 1, finalKb);
    Vector velocity = direction.clone().setY(0).normalize().multiply(mul);
    velocity.setY(isLinear ? direction.getY() : kbY);

    if (factorsPreviousVelocity) {
      velocity = victim.getVelocity().add(velocity);
    }

    return Optional.of(velocity);
  }

  public double getFinalKb(Damageable victim) {
    double kbVal = kb;

    if (factorsHealth) {
      kbVal *= MathUtils.decVal(minHealthBoost,
          maxHealthBoost,
          victim.getMaxHealth(),
          victim.getHealth()
      );
    }

    if (factorsKit && victim instanceof Player) {
      Kit kit = SSL.getInstance().getKitManager().getSelectedKit((Player) victim);

      if (kit != null) {
        kbVal *= kit.getKb();
      }
    }

    return kbVal;
  }
}
