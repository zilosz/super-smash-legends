package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Thunderbolt extends ChargedRightClickAbility {

    @Override
    public void onChargeTick() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.CREEPER_HISS, 1, 2);
    }

    @Override
    public void onFailedCharge() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.AMBIENCE_THUNDER, 1, 2);
    }

    @Override
    public void onSuccessfulCharge() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.AMBIENCE_THUNDER, 2, 0.5f);

        EntityFinder finder = new EntityFinder(new HitBoxSelector(this.config.getDouble("HitBox")));

        int ticks = this.ticksCharging - this.minChargeTicks;
        int max = this.maxChargeTicks - this.minChargeTicks;

        double damage = YamlReader.incLin(this.config, "Damage", ticks, max);
        double kb = YamlReader.incLin(this.config, "Kb", ticks, max);
        double range = YamlReader.incLin(this.config, "Range", ticks, max);

        Location location = this.player.getEyeLocation();
        Vector step = location.getDirection().multiply(0.25);

        boolean found = false;
        double stepped = 0;

        while (true) {

            if (stepped > range || location.getBlock().getType().isSolid() || found) {
                this.endEffect(location);
                break;
            }

            for (LivingEntity target : finder.findAll(this.player, location)) {

                AttackSettings settings = new AttackSettings(this.config, step)
                        .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                        .modifyKb(kbSettings -> kbSettings.setKb(kb));

                if (SSL.getInstance().getDamageManager().attack(target, this, settings)) {
                    found = true;
                    break;
                }
            }

            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).show(location);

            stepped += 0.25;
            location.add(step);
        }
    }

    private void endEffect(Location location) {
        location.getWorld().strikeLightningEffect(location);
        location.getWorld().playSound(location, Sound.AMBIENCE_THUNDER, 4, 0.5f);
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).boom(SSL.getInstance(), location, 5, 0.5, 18);
    }
}
