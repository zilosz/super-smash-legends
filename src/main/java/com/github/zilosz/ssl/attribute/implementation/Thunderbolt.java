package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

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

        int ticks = this.ticksCharging - this.getMinChargeTicks();
        int max = this.getMaxChargeTicks() - this.getMinChargeTicks();

        double damage = YamlReader.increasingValue(this.config, "Damage", ticks, max);
        double kb = YamlReader.increasingValue(this.config, "Kb", ticks, max);
        double range = YamlReader.increasingValue(this.config, "Range", ticks, max);

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
                Attack attack = YamlReader.attack(this.config, step);
                attack.getDamage().setDamage(damage);
                attack.getKb().setKb(kb);

                if (SSL.getInstance().getDamageManager().attack(target, this, attack)) {
                    found = true;
                    break;
                }
            }

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(255, 255, 0));
            new ParticleMaker(particle).show(location);

            stepped += 0.25;
            location.add(step);
        }
    }

    private void endEffect(Location location) {
        location.getWorld().strikeLightningEffect(location);
        location.getWorld().playSound(location, Sound.AMBIENCE_THUNDER, 4, 0.5f);

        BlockHitResult result = BlockUtils.findBlockHitByBox(location, 1, 1, 1, 0.25);
        BlockFace face = result == null ? null : result.getFace();

        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE).setColor(new Color(255, 255, 0));
        new ParticleMaker(particle).setFace(face).boom(SSL.getInstance(), location, 5, 0.5, 18);
    }
}
