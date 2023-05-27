package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.RangeSelector;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

public class Earthquake extends RightClickAbility {
    private BukkitTask quakeTask;
    private BukkitTask uprootTask;
    private BukkitTask stopTask;

    public Earthquake(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1, 0.5f);

        this.quakeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!EntityUtils.isPlayerGrounded(player)) return;

            Location location = player.getLocation().add(0, 0.3, 0);
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(139, 69, 19).ring(location, 90, 0, 1.5, 30);
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(160, 82, 45).ring(location, 90, 0, 0.75, 15);

            RangeSelector selector = new HitBoxSelector(
                    config.getDouble("HorizontalRange"), config.getDouble("VerticalRange"), config.getDouble("HorizontalRange"));

            new EntityFinder(plugin, selector).findAll(player).forEach(target -> {
                if (!target.isOnGround()) return;

                Damage damage = Damage.Builder.fromConfig(config, VectorUtils.fromTo(player, target)).build();

                if (plugin.getDamageManager().attemptAttributeDamage(target, damage, this)) {
                    player.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    uproot(target.getLocation());
                }
            });
        }, 0, config.getInt("UprootInterval"));

        this.uprootTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!EntityUtils.isPlayerGrounded(player)) return;

            Location center = player.getLocation();

            int x = (int) MathUtils.randSpread(center.getBlockX(), config.getDouble("HorizontalRange"));
            int z = (int) MathUtils.randSpread(center.getBlockZ(), config.getDouble("HorizontalRange"));

            Location currLoc = new Location(player.getWorld(), x, center.getBlockY() + config.getDouble("VerticalRange"), z);
            int movedDown = 0;

            while (movedDown < config.getDouble("VerticalRange") * 2 && currLoc.getBlock().getType() == Material.AIR) {
                currLoc.subtract(0, 1, 0);
                movedDown++;
            }

            Location uprootLocation = new Location(player.getWorld(), x, currLoc.getY() + 1, z);

            if (uprootLocation.getBlock().getType() == Material.AIR) {
                uproot(uprootLocation);
            }
        }, 0, config.getInt("UprootInterval"));

        this.stopTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startCooldown();
            this.quakeTask.cancel();
            this.uprootTask.cancel();
            stopTask = null;
            player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 1);
        }, config.getInt("Duration"));
    }

    private void uproot(Location loc) {
        player.getWorld().playSound(loc, Sound.DIG_GRASS, 1, 1);

        Block groundBlock = loc.clone().subtract(0, 0.5, 0).getBlock();
        BlockUtils.setBlockFast(loc, groundBlock.getTypeId(), groundBlock.getData());

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                BlockUtils.setBlockFast(loc, Material.AIR.getId(), (byte) 2), config.getInt("UprootDuration"));
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.stopTask != null) {
            this.stopTask.cancel();
            this.stopTask = null;
            this.quakeTask.cancel();
            this.uprootTask.cancel();
        }
    }
}
