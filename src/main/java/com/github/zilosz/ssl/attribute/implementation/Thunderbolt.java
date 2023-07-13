package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.ChargedRightClickAbility;
import com.github.zilosz.ssl.utils.effects.Effects;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

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
                Attack attack = YamlReader.attack(this.config, step, this.getDisplayName());
                attack.getDamage().setDamage(damage);
                attack.getKb().setKb(kb);

                AttackInfo attackInfo = new AttackInfo(AttackType.THUNDERBOLT, this);

                if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
                    found = true;
                    break;
                }
            }

            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE)
                    .setColor(this.kit.getColor().getAwtColor());
            new ParticleMaker(particle).show(location);

            stepped += 0.25;
            location.add(step);
        }
    }

    private void endEffect(Location location) {
        location.getWorld().strikeLightningEffect(location);
        location.getWorld().playSound(location, Sound.AMBIENCE_THUNDER, 4, 0.5f);

        FireworkEffect.Builder settings = FireworkEffect.builder()
                .withColor(this.kit.getColor().getBukkitColor())
                .with(FireworkEffect.Type.BALL)
                .withTrail();

        Firework firework = Effects.launchFirework(location, settings, 1);
        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), firework::detonate, 5);
    }
}
