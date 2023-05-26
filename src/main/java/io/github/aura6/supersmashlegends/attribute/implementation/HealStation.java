package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickBlockAbility;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.event.RegenEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.TeamManager;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HealStation extends RightClickBlockAbility {
    private double stationHealth;
    private Block standBlock;
    private ArmorStand stand;
    private BukkitTask particleTask;
    private HashMap<Block, Tuple<Material, Byte>> replacedBlocks;

    public HealStation(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || event.getBlockFace() != BlockFace.UP || isActive();
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        stationHealth = config.getDouble("Health");

        Location clickedLocation = event.getClickedBlock().getLocation();
        Location standLocation = clickedLocation.clone().add(0, 1, 0);

        standBlock = standLocation.getBlock();
        standBlock.setType(Material.BREWING_STAND);

        stand = player.getWorld().spawn(standLocation, ArmorStand.class);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        updateStationHealth();

        replacedBlocks = new HashMap<>();

        double minX = standLocation.getBlockX() - config.getInt("Radius");
        double maxX = standLocation.getBlockX() + config.getInt("Radius");

        double minZ = standLocation.getBlockZ() - config.getInt("Radius");
        double maxZ = standLocation.getBlockZ() + config.getInt("Radius");

        for (double x = minX; x <= maxX; x++) {

            for (double z = minZ; z <= maxZ; z++) {
                Location loc = new Location(player.getWorld(), x, clickedLocation.getY(), z);
                Block block = loc.getBlock();

                if (block.getType() != Material.AIR) {
                    replacedBlocks.put(block, new Tuple<>(block.getType(), block.getData()));
                    BlockUtils.setBlockFast(loc, Material.EMERALD_BLOCK);
                }
            }
        }

        List<Location> particleLocations = new ArrayList<>();

        double outlineGap = Math.abs(2 * config.getInt("Radius") + 1) / config.getDouble("ParticlesPerLine");
        double x = standLocation.getBlockX() - config.getInt("Radius") + 0.5;

        for (int i = 0; i < config.getDouble("ParticlesPerLine"); i++) {
            double z = standLocation.getBlockZ() - config.getInt("Radius") + 0.5;

            for (int j = 0; j < config.getDouble("ParticlesPerLine"); j++) {

                if (i == 0 || i == config.getDouble("ParticlesPerLine") - 1 || j == 0 || j == config.getDouble("ParticlesPerLine") - 1) {
                    particleLocations.add(new Location(player.getWorld(), x, standLocation.getBlockY() + 1.5, z));
                }

                z += outlineGap;
            }

            x += outlineGap;
        }

        ParticleBuilder particle = new ParticleBuilder(EnumParticle.VILLAGER_HAPPY);
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> particleLocations.forEach(particle::show), 4, 4);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (isActive()) {
                destroyStation();
            }
        }, config.getInt("Duration"));
    }

    private void updateStationHealth() {
        stand.setCustomName(MessageUtils.progressBar((int) Math.ceil(stationHealth), config.getInt("Health"), 10, "&a|", "&c|"));
    }

    private boolean isActive() {
        return stand != null && stand.isValid();
    }

    private void destroyStation() {
        startCooldown();
        stand.remove();
        standBlock.setType(Material.AIR);
        particleTask.cancel();
        replacedBlocks.forEach((block, state) -> BlockUtils.setBlockFast(block.getLocation(), state.a(), state.b()));
    }

    @EventHandler
    public void onRegen(RegenEvent event) {
        if (!isActive()) return;

        Player other = event.getPlayer();

        if (other.getHealth() == 20) return;

        TeamManager teamManager = plugin.getTeamManager();

        if (teamManager.getPlayerTeam(other) != teamManager.getPlayerTeam(player)) return;

        if (replacedBlocks.containsKey(other.getLocation().subtract(0, 1, 0).getBlock())) {
            event.setRegen(event.getRegen() * config.getDouble("RegenMultiplier"));
            player.getWorld().playSound(event.getPlayer().getLocation(), Sound.PIG_IDLE, 2, 2);

            Location particleLoc = EntityUtils.top(other).add(0, 0.4, 0);

            for (int i = 0; i < 10; i++) {
                new ParticleBuilder(EnumParticle.HEART).show(particleLoc);
            }
        }
    }

    @EventHandler
    public void onDamage(DamageEvent event) {
        if (event.getVictim() != stand) return;

        event.setCancelled(true);

        player.getWorld().playSound(stand.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
        stationHealth -= event.getDamage().getDamage();

        if (stationHealth < 0) {
            destroyStation();

        } else {
            updateStationHealth();
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        if (isActive()) {
            destroyStation();
        }
    }
}
