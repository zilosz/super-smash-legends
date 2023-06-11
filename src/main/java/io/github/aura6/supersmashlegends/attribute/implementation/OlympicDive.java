package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.EntitySelector;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class OlympicDive extends RightClickAbility {
    private BukkitTask task;

    private DiveState diveState = DiveState.INACTIVE;

    private enum DiveState {
        INACTIVE,
        ASCENDING,
        DIVING
    }

    public OlympicDive(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void dive() {
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

            Vector direction = VectorUtils.fromTo(this.player, target);
            Damage damageObj = Damage.Builder.fromConfig(this.config, direction).setDamage(damage).setKb(kb).build();

            this.plugin.getDamageManager().attemptAttributeDamage(target, damageObj, this);
        });
    }

    private void reset(boolean natural) {
        if (this.diveState == DiveState.INACTIVE) return;

        this.diveState = DiveState.INACTIVE;
        this.task.cancel();

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
    public void onAttributeDamage(AttributeDamageEvent event) {
        Attribute attr = event.getAttribute();

        if (attr.getPlayer() == this.player && this.diveState != DiveState.INACTIVE && attr instanceof Melee) {
            event.getDamage().setKb(0);
            event.getDamage().setKbY(0);
        }
    }
}
