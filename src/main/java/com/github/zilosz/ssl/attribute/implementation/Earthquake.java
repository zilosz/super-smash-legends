package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.util.block.BlockUtils;
import com.github.zilosz.ssl.util.block.MaterialInfo;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.implementation.HitBoxSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.MathUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
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

            for (LivingEntity target : new EntityFinder(selector).findAll(this.player)) {
                if (!target.isOnGround()) continue;

                Vector direction = VectorUtils.fromTo(this.player, target);
                Attack attack = YamlReader.attack(this.config, direction, this.getDisplayName());
                AttackInfo attackInfo = new AttackInfo(AttackType.EARTHQUAKE, this);

                if (SSL.getInstance().getDamageManager().attack(target, attack, attackInfo)) {
                    this.player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    this.uproot(target.getLocation());
                }
            }
        }, 0, this.config.getInt("UprootInterval"));

        this.stopTask = Bukkit.getScheduler()
                .runTaskLater(SSL.getInstance(), this::reset, this.config.getInt("Duration"));
    }

    private void uproot(Location loc) {
        this.player.getWorld().playSound(loc, Sound.DIG_GRASS, 1, 1);

        Block groundBlock = loc.clone().subtract(0, 0.5, 0).getBlock();
        MaterialInfo airInfo = MaterialInfo.fromBlock(loc.getBlock());
        BlockUtils.setBlockFast(loc, MaterialInfo.fromBlock(groundBlock));

        int duration = this.config.getInt("UprootDuration");
        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> BlockUtils.setBlockFast(loc, airInfo), duration);
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
