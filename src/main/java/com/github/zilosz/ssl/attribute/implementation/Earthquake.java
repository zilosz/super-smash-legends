package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import com.github.zilosz.ssl.utils.effect.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;

public class Earthquake extends RightClickAbility {
    private BukkitTask quakeTask;
    @Nullable private BukkitTask stopTask;

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.stopTask != null;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_THROW, 1, 0.5f);

        double horizontal = this.config.getDouble("HorizontalRange");
        double vertical = this.config.getDouble("VerticalRange");

        this.quakeTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            if (!EntityUtils.isPlayerGrounded(this.player)) return;

            Location center = this.player.getLocation();

            int x = (int) MathUtils.randSpread(center.getBlockX(), horizontal);
            int z = (int) MathUtils.randSpread(center.getBlockZ(), horizontal);

            Location currLoc = new Location(this.player.getWorld(), x, center.getBlockY() + vertical, z);
            int movedDown = 0;

            while (movedDown < vertical * 2 && currLoc.getBlock().getType() == Material.AIR) {
                currLoc.subtract(0, 1, 0);
                movedDown++;
            }

            Location uprootLocation = new Location(this.player.getWorld(), x, currLoc.getY() + 1, z);

            if (uprootLocation.getBlock().getType() == Material.AIR) {
                this.uproot(uprootLocation);
            }

            Location location = this.player.getLocation().add(0, 0.3, 0);

            ParticleBuilder largeParticle = new ParticleBuilder(ParticleEffect.REDSTONE)
                    .setColor(new Color(139, 69, 19));
            new ParticleMaker(largeParticle).ring(location, 90, 0, 1.5, 30);

            ParticleBuilder smallParticle = new ParticleBuilder(ParticleEffect.REDSTONE)
                    .setColor(new Color(160, 82, 45));
            new ParticleMaker(smallParticle).ring(location, 90, 0, 0.75, 15);

            EntitySelector selector = new HitBoxSelector(horizontal, vertical, horizontal);

            new EntityFinder(selector).findAll(this.player).forEach(target -> {
                if (!target.isOnGround()) return;

                Attack settings = new Attack(this.config, VectorUtils.fromTo(this.player, target));

                if (SSL.getInstance().getDamageManager().attack(target, this, settings)) {
                    this.player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    this.uproot(target.getLocation());
                }
            });
        }, 0, this.config.getInt("UprootInterval"));

        this.stopTask = Bukkit.getScheduler()
                .runTaskLater(SSL.getInstance(), this::reset, this.config.getInt("Duration"));
    }

    private void uproot(Location loc) {
        this.player.getWorld().playSound(loc, Sound.DIG_GRASS, 1, 1);

        Block groundBlock = loc.clone().subtract(0, 0.5, 0).getBlock();
        BlockUtils.setBlockFast(loc, groundBlock.getTypeId(), groundBlock.getData());

        int id = Material.AIR.getId();
        int duration = this.config.getInt("UprootDuration");

        Bukkit.getScheduler()
                .runTaskLater(SSL.getInstance(), () -> BlockUtils.setBlockFast(loc, id, (byte) 2), duration);
    }

    private void reset() {
        if (this.stopTask == null) return;

        this.stopTask.cancel();
        this.stopTask = null;
        this.quakeTask.cancel();
        this.startCooldown();

        this.player.getWorld().playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 1);
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }
}
