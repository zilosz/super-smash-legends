package com.github.zilosz.ssl.attack;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DamageIndicator extends BukkitRunnable {
  private final Hologram hologram;
  private final TextHologramLine line;
  private final LivingEntity entity;
  private final double height;

  public static DamageIndicator create(LivingEntity entity, double damage) {
    Section heightConfig =
        SSL.getInstance().getResources().getConfig().getSection("Damage.Indicator.Height");
    double height = heightConfig.getDouble(entity instanceof Player ? "Player" : "Entity");

    Location loc = relativePosition(entity, height);
    Hologram hologram = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(loc);
    TextHologramLine line = hologram.getLines().appendText(damageFormat(entity, damage));

    DamageIndicator indicator = new DamageIndicator(hologram, line, entity, height);
    indicator.runTaskTimer(SSL.getInstance(), 0, 0);

    if (entity instanceof Player) {
      hologram
          .getVisibilitySettings()
          .setIndividualVisibility((Player) entity, VisibilitySettings.Visibility.HIDDEN);
    }

    return indicator;
  }

  private static Location relativePosition(Entity entity, double height) {
    return EntityUtils.top(entity).add(0, height, 0);
  }

  private static String damageFormat(Damageable entity, double damage) {
    double percentHealthLeft = Math.max(0, (entity.getHealth() - damage) / entity.getMaxHealth());
    String color;

    if (percentHealthLeft < 0.25) {
      color = "&c";

    }
    else if (percentHealthLeft < 0.5) {
      color = "&6";

    }
    else if (percentHealthLeft < 0.75) {
      color = "&e";

    }
    else {
      color = "&a";
    }

    String damageString = new DecimalFormat("#.#").format(percentHealthLeft * 10);
    return MessageUtils.color(color + damageString + "&7/10");
  }

  public void setVisibility(VisibilitySettings.Visibility visibility) {
    hologram.getVisibilitySettings().setGlobalVisibility(visibility);
  }

  public void updateDamage(double damage) {
    line.setText(damageFormat(entity, damage));
  }

  @Override
  public void run() {
    hologram.setPosition(relativePosition(entity, height));

    if (!entity.isValid()) {
      destroy();
    }
  }

  public void destroy() {
    cancel();
    hologram.delete();
  }
}
