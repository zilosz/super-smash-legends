package io.github.aura6.supersmashlegends.damage;

import io.github.aura6.supersmashlegends.utils.Hologram;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;

public abstract class DamageIndicator extends Hologram {
    private final LivingEntity entity;
    private double damage = 0;

    public DamageIndicator(Plugin plugin, LivingEntity entity) {
        super(plugin);
        this.entity = entity;
    }

    public void stackDamage(double damage) {
        this.damage += damage;
        double healthLeft = entity.getHealth() / entity.getMaxHealth();
        String color;

        if (healthLeft < 0.25) {
            color = "&c";

        } else if (healthLeft < 0.5) {
            color = "&6";

        } else if (healthLeft < 0.75) {
            color = "&e";

        } else {
            color = "&a";
        }

        setText(MessageUtils.color(color + new DecimalFormat("#.#").format(this.damage)));
    }
}
