package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Thunderbolt extends ChargedRightClickAbility {

    public Thunderbolt(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 1, 2);
    }

    private void endEffect(Location location) {
        location.getWorld().strikeLightningEffect(location);
        location.getWorld().playSound(location, Sound.AMBIENCE_THUNDER, 4, 0.5f);
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).boom(plugin, location, 5, 0.5, 18);
    }

    @Override
    public void onSuccessfulCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 2, 0.5f);

        EntityFinder finder = new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox")));

        int ticks = ticksCharging - minChargeTicks;
        int max = maxChargeTicks - minChargeTicks;

        double damage = YamlReader.incLin(config, "Damage", ticks, max);
        double kb = YamlReader.incLin(config, "Kb", ticks, max);
        double range = YamlReader.incLin(config, "Range", ticks, max);

        Location location = player.getEyeLocation();
        Vector step = location.getDirection().multiply(0.25);

        boolean found = false;
        double stepped = 0;

        while (true) {

            if (stepped > range || location.getBlock().getType().isSolid() || found) {
                endEffect(location);
                break;
            }

            for (LivingEntity target : finder.findAll(player, location)) {

                AttackSettings settings = new AttackSettings(this.config, step)
                        .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                        .modifyKb(kbSettings -> kbSettings.setKb(kb));

                if (plugin.getDamageManager().attack(target, this, settings)) {
                    found = true;
                    break;
                }
            }

            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).show(location);

            stepped += 0.25;
            location.add(step);
        }
    }

    @Override
    public void onFailedCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1, 2);
    }
}
