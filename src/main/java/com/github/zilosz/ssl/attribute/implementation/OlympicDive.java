package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class OlympicDive extends RightClickAbility {
    private BukkitTask task;
    private BukkitTask diveDelayer;
    private boolean canDive = false;
    private State diveState = State.INACTIVE;

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (this.diveState) {

            case INACTIVE:
                this.ascend();
                break;

            case ASCENDING:
                this.dive();
                break;
        }
    }

    private void ascend() {
        this.sendUseMessage();

        this.diveState = State.ASCENDING;

        this.player.setVelocity(new Vector(0, this.config.getDouble("AscendVelocity"), 0));
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 0.5f, 2);

        EntitySelector selector = new DistanceSelector(this.config.getDouble("PullDistance"));
        EntityFinder finder = new EntityFinder(selector).setTeamPreference(TeamPreference.ANY);

        finder.findAll(this.player).forEach(target -> {
            Vector pullDirection = VectorUtils.fromTo(target, this.player).normalize();
            Vector extraY = new Vector(0, this.config.getDouble("ExtraPullY"), 0);
            Vector velocity = pullDirection.multiply(this.config.getDouble("PullVelocity")).add(extraY);
            velocity.setY(Math.max(velocity.getY(), this.config.getDouble("MaxPullY")));
            target.setVelocity(velocity);
        });

        int diveDelay = this.config.getInt("DiveDelay");
        this.diveDelayer = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> this.canDive = true, diveDelay);

        this.task = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (EntityUtils.isPlayerGrounded(this.player)) {

                switch (this.diveState) {

                    case ASCENDING:
                        this.reset(true);
                        break;

                    case DIVING:
                        this.reset(true);
                        this.onDiveFinish();
                }

            } else if (this.diveState == State.ASCENDING) {

                for (int i = 0; i < 10; i++) {
                    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.DRIP_WATER);
                    new ParticleMaker(particle).setSpread(0.5).show(this.player.getLocation());
                }
            }
        }, 4, 0);
    }

    private void dive() {
        if (!this.canDive) return;

        this.diveState = State.DIVING;

        double diveVelocity = this.config.getDouble("DiveVelocity");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(diveVelocity));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_THROW, 3, 0.5f);
    }

    private void reset(boolean natural) {
        if (this.diveState == State.INACTIVE) return;

        this.diveState = State.INACTIVE;
        this.task.cancel();
        this.canDive = false;

        if (this.diveDelayer != null) {
            this.diveDelayer.cancel();
        }

        if (natural) {
            this.player.playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 0.5f, 2);
            this.startCooldown();
        }
    }

    private void onDiveFinish() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 1, 1);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.EXPLODE, 0.5f, 2);

        for (int i = 0; i < 10; i++) {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
            new ParticleMaker(particle).setSpread(3.5, 0.6, 3.5).show(this.player.getLocation());
        }

        double radius = this.config.getDouble("DiveDamageRadius");
        EntitySelector selector = new DistanceSelector(radius);

        new EntityFinder(selector).findAll(this.player).forEach(target -> {
            double distance = target.getLocation().distance(this.player.getLocation());
            double damage = YamlReader.getDecreasingValue(this.config, "DiveDamage", distance, radius);
            double kb = YamlReader.getDecreasingValue(this.config, "DiveKb", distance, radius);

            Attack attack = new Attack(this.config, VectorUtils.fromTo(this.player, target));
            attack.getDamage().setDamage(damage);
            attack.getKb().setKb(kb);

            SSL.getInstance().getDamageManager().attack(target, this, attack);
        });
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
    }

    @EventHandler
    public void onKb(AttributeKbEvent event) {
        Attribute attr = event.getAttribute();

        if (attr.getPlayer() == this.player && this.diveState != State.INACTIVE && attr instanceof Melee) {
            event.getKb().setDirection(null);
        }
    }

    private enum State {
        INACTIVE,
        ASCENDING,
        DIVING
    }
}
