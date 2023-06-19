package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.file.YamlReader;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class OlympicDive extends RightClickAbility {
    private BukkitTask task;
    private BukkitTask diveDelayer;
    private boolean canDive = false;
    private DiveState diveState = DiveState.INACTIVE;

    private enum DiveState {
        INACTIVE,
        ASCENDING,
        DIVING
    }

    public OlympicDive(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void dive() {
        if (!this.canDive) return;

        this.diveState = DiveState.DIVING;

        double diveVelocity = this.config.getDouble("DiveVelocity");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(diveVelocity));

        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_THROW, 3, 0.5f);
    }

    private void onDiveFinish() {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 1, 1);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.EXPLODE, 0.5f, 2);

        for (int i = 0; i < 10; i++) {
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).setSpread(3.5f, 0.6f, 3.5f).show(this.player.getLocation());
        }

        double radius = this.config.getDouble("DiveDamageRadius");
        EntitySelector selector = new DistanceSelector(radius);

        new EntityFinder(this.plugin, selector).findAll(this.player).forEach(target -> {
            double distance = target.getLocation().distance(this.player.getLocation());
            double damage = YamlReader.decLin(this.config, "DiveDamage", distance, radius);
            double kb = YamlReader.decLin(this.config, "DiveKb", distance, radius);

            AttackSettings settings = new AttackSettings(this.config, VectorUtils.fromTo(this.player, target))
                    .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                    .modifyKb(kbSettings -> kbSettings.setKb(kb));

            this.plugin.getDamageManager().attack(target, this, settings);
        });
    }

    private void reset(boolean natural) {
        if (this.diveState == DiveState.INACTIVE) return;

        this.diveState = DiveState.INACTIVE;
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

    private void ascend() {
        this.sendUseMessage();

        this.diveState = DiveState.ASCENDING;

        this.player.setVelocity(new Vector(0, this.config.getDouble("AscendVelocity"), 0));
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPLASH, 0.5f, 2);

        EntitySelector selector = new DistanceSelector(this.config.getDouble("PullDistance"));
        EntityFinder finder = new EntityFinder(this.plugin, selector).setTeamPreference(TeamPreference.ANY);

        finder.findAll(this.player).forEach(target -> {
            Vector pullDirection = VectorUtils.fromTo(target, this.player).normalize();
            Vector extraY = new Vector(0, this.config.getDouble("ExtraPullY"), 0);
            Vector velocity = pullDirection.multiply(this.config.getDouble("PullVelocity")).add(extraY);
            velocity.setY(Math.max(velocity.getY(), this.config.getDouble("MaxPullY")));
            target.setVelocity(velocity);
        });

        int diveDelay = this.config.getInt("DiveDelay");
        this.diveDelayer = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.canDive = true, diveDelay);

        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (EntityUtils.isPlayerGrounded(this.player)) {

                switch (this.diveState) {

                    case ASCENDING:
                        this.reset(true);
                        break;

                    case DIVING:
                        this.reset(true);
                        this.onDiveFinish();
                }

            } else if (this.diveState == DiveState.ASCENDING) {

                for (int i = 0; i < 10; i++) {
                    new ParticleBuilder(EnumParticle.DRIP_WATER).setSpread(0.5f, 0.5f, 0.5f).show(this.player.getLocation());
                }
            }
        }, 4, 0);
    }

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

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset(false);
    }

    @EventHandler
    public void onKb(AttributeKbEvent event) {
        Attribute attr = event.getAttribute();

        if (attr.getPlayer() == this.player && this.diveState != DiveState.INACTIVE && attr instanceof Melee) {
            event.getKbSettings().setDirection(null);
        }
    }
}
