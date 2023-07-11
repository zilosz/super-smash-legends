package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.NoteColor;

import java.util.List;

public class SpookyScream extends RightClickAbility {
    private int currRing = 0;
    private double radius;
    private BukkitTask ringTask;
    private BukkitTask attackTask;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.currRing > 0;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.resetActiveScream();
    }

    private void resetActiveScream() {
        this.currRing = 0;

        if (this.ringTask != null) {
            this.ringTask.cancel();
            this.attackTask.cancel();
        }
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location currLocation = this.player.getEyeLocation();

        this.player.getWorld().playSound(currLocation, Sound.WITHER_SHOOT, 1, 0.8f);
        this.player.getWorld().playSound(currLocation, Sound.GHAST_SCREAM, 0.5f, 2);

        Vector step = currLocation.getDirection().multiply(this.config.getDouble("RingGap"));

        this.radius = this.config.getDouble("Radius");
        List<Integer> notes = this.config.getIntList("Notes");

        this.ringTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            currLocation.add(step);

            int note = CollectionUtils.selectRandom(notes);
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.NOTE).setParticleData(new NoteColor(note));
            new ParticleMaker(particle).ring(currLocation, this.radius, this.config.getDouble("DegreeGap"));

            if (++this.currRing == this.config.getInt("Rings")) {
                this.resetActiveScream();
                this.startCooldown();

            } else {
                this.radius += this.config.getDouble("RadiusGrowthPerTick");
            }
        }, 0, this.config.getInt("TicksPerRing"));

        this.attackTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            double searchRadius = this.radius + this.config.getDouble("ExtraHitBox");

            new EntityFinder(new DistanceSelector(searchRadius)).findAll(this.player, currLocation).forEach(target -> {
                boolean isPumpkin = target.hasMetadata("pumpkin");
                String attackPath = isPumpkin ? "PumpkinAttack" : "NormalAttack";
                Attack attack = YamlReader.attack(this.config.getSection(attackPath), step);

                if (SSL.getInstance().getDamageManager().attack(target, this, attack)) {
                    this.player.playSound(this.player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    target.getWorld().playSound(target.getLocation(), Sound.WITHER_HURT, 1, 1);

                    PotionEffect effect = YamlReader.potionEffect(this.config.getSection("Wither"));
                    PotionEffectEvent.fromPotionEffect(target, effect).apply();

                    if (isPumpkin) {
                        target.getWorld().playSound(target.getLocation(), Sound.WITHER_HURT, 1, 1);

                        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
                        new ParticleMaker(particle).show(target.getLocation());
                    }
                }
            });
        }, 0, 0);
    }
}
