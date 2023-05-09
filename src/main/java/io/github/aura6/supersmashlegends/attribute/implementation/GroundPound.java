package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.HitBoxSelector;
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

    public GroundPound(SuperSmashLegends plugin, Section config, Kit kit) {
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
            Vector direction = player.getLocation().getDirection();
            Damage dmg = Damage.Builder.fromConfig(config, direction).setDamage(damage).setKb(kb).build();
            plugin.getDamageManager().attemptAttributeDamage(target, dmg, this);

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
    public void destroy() {
        super.destroy();
        resetFall(true);
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
        if (event.getVictim() == player && fallTask != null) {
            event.getDamage().setDirection(null);
        }
    }
}
