package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.event.attack.AttributeKbEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.HitBoxSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class GroundPound extends RightClickAbility {
    private BukkitTask fallTask;
    private BukkitTask checkAirborneTask;

    public GroundPound(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || fallTask != null;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 2, 1);

        player.setVelocity(new Vector(0, -config.getDouble("DownwardVelocity"), 0));
        double initialHeight = player.getLocation().getY();
        fallTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> onRun(initialHeight), 0, 0);

        if (checkAirborneTask == null) {
            checkAirborneTask = Bukkit.getScheduler().runTaskTimer(plugin, this::onGroundCheck, 0, 0);
        }
    }

    private void onGroundCheck() {
        if (!EntityUtils.isPlayerGrounded(player)) return;

        if (fallTask != null) {
            player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_HIT, 2, 0.5f);
        }

        startCooldown();
        resetFall(true);
    }

    private void onRun(double initialHeight) {
        new ParticleBuilder(EnumParticle.REDSTONE).ring(EntityUtils.center(player), 90, 0, 1, 10);

        double fallen = Math.max(0, initialHeight - player.getLocation().getY());
        double damage = YamlReader.incLin(config, "Damage", fallen, config.getDouble("MaxFall"));
        double kb = YamlReader.incLin(config, "Kb", fallen, config.getDouble("MaxFall"));

        boolean foundTarget = false;
        EntityFinder finder = new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox")));

        for (LivingEntity target : finder.findAll(player)) {

            AttackSettings settings = new AttackSettings(this.config, this.player.getLocation().getDirection())
                    .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                    .modifyKb(kbSettings -> kbSettings.setKb(kb));

            plugin.getDamageManager().attack(target, this, settings);

            player.getWorld().playSound(target.getLocation(), Sound.EXPLODE, 2, 2);
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(target.getLocation());

            foundTarget = true;
        }

        if (foundTarget) {
            resetFall(false);
            kit.getJump().giveExtraJumps(1);
            double bounce = YamlReader.incLin(config, "Bounce", fallen, config.getDouble("MaxFall"));
            player.setVelocity(new Vector(0, bounce, 0));
        }
    }

    private void resetFall(boolean stopGroundTask) {

        if (fallTask != null) {
            fallTask.cancel();
        }

        fallTask = null;

        if (stopGroundTask && checkAirborneTask != null) {
            checkAirborneTask.cancel();
            checkAirborneTask = null;
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        resetFall(true);
    }

    @EventHandler
    public void onKb(AttributeKbEvent event) {
        if (event.getVictim() == player && fallTask != null) {
            event.getKbSettings().setDirection(null);
        }
    }
}
