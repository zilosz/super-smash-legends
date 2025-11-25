package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@Getter
@Setter
@AllArgsConstructor
public class Damage {
  private double damage;
  private boolean factorsArmor;

  public double getFinalDamage(LivingEntity victim) {
    double damageFinal = damage;

    if (factorsArmor && victim instanceof Player) {
      Kit kit = SSL.getInstance().getKitManager().getSelectedKit((Player) victim);

      if (kit != null) {
        damageFinal *= kit.getArmor();
      }
    }

    return damageFinal;
  }
}
