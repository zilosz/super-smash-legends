package io.github.aura6.supersmashlegends.power;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.CollectionUtils;
import io.github.aura6.supersmashlegends.utils.Reflector;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public class PowerManager implements Listener {
    private final SuperSmashLegends plugin;

    private final List<Class<? extends Ability>> powerClasses = new ArrayList<>();
    private final List<PowerInfo> powerInfoList = new ArrayList<>();
    private final Set<PowerCrystal> activeCrystals = new HashSet<>();
    private final Set<Location> occupiedLocations = new HashSet<>();
    private final Map<Kit, List<Attribute>> givenPowerUps = new HashMap<>();

    private BukkitTask powerTimer;
    private BukkitTask powerSpawnTask;
    private BukkitTask soundTask;
    private BukkitTask effectTask;

    @SuppressWarnings("unchecked")
    public PowerManager(SuperSmashLegends plugin) {
        this.plugin = plugin;

        Section powerSection = getConfig().getSection("Powers");

        powerSection.getKeys().forEach(key -> {
            String name = getClass().getPackageName() + ".implementation." + key;
            Class<? extends Ability> clazz = (Class<? extends Ability>) Reflector.loadClass(name);
            powerClasses.add(clazz);
            powerInfoList.add(new PowerInfo(powerSection.getSection((String) key)));
        });
    }

    private Section getConfig() {
        return plugin.getResources().getConfig().getSection("Power");
    }

    public void startPowerTimer() {
        powerTimer = Bukkit.getScheduler().runTaskLater(plugin, this::spawnPower, getConfig().getInt("Frequency"));
    }

    public void spawnPower() {
        startPowerTimer();

        if (powerInfoList.size() == 0) return;

        int totalRarity = powerInfoList.stream().mapToInt(PowerInfo::getRarity).sum();
        double random = Math.random();

        int i;

        for (i = 0; i < powerInfoList.size(); i++) {

            if (random * totalRarity - powerInfoList.get(i).getRarity() <= 0) {
                break;
            }
        }

        PowerInfo powerInfo = powerInfoList.get(i);
        Class<? extends Ability> powerClass = powerClasses.get(i);

        List<Location> possibleLocations = plugin.getArenaManager().getArena().getPowerLocations();
        possibleLocations.removeIf(occupiedLocations::contains);

        if (possibleLocations.isEmpty()) return;

        Chat.POWER.broadcast("&7A power is spawning!");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 3, 0.75f);
        }

        Location powerLocation = CollectionUtils.selectRandom(possibleLocations);
        occupiedLocations.add(powerLocation);

        double height = getConfig().getDouble("Height") + getConfig().getDouble("DropSpeed");
        Location moveLocation = powerLocation.clone().add(0, height, 0);

        Location beamLoc = moveLocation.clone();
        List<Location> beamLocations = new ArrayList<>();

        while (beamLoc.getY() > powerLocation.getY()) {
            beamLocations.add(beamLoc.clone());
            beamLoc.subtract(0, 0.5, 0);
        }

        soundTask = Bukkit.getScheduler().runTaskTimer(plugin, () ->
                moveLocation.getWorld().playSound(moveLocation, Sound.FIREWORK_TWINKLE, 10, 1), 0, 10);

        effectTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Location current = moveLocation.subtract(0, getConfig().getDouble("DropSpeed"), 0);

            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(current);
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(75, 0, 130).setSpread(0.2f, 0.2f, 0.2f).setFace(BlockFace.UP).boom(plugin, current, 5, 0.5, 10);

            for (Location beam : beamLocations) {
                new ParticleBuilder(EnumParticle.REDSTONE).setRgb(231, 34, 128).setSpread(0.15f, 0.15f, 0.15f).show(beam);
            }
        }, 0, 0);

        powerSpawnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeCrystals.add(new PowerCrystal(moveLocation, powerLocation, powerClass, powerInfo));

            effectTask.cancel();
            soundTask.cancel();

            Chat.POWER.broadcast(String.format("&7A %s &7has spawned!", powerInfo.getName()));
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ANVIL_LAND, 2, 2));
        }, (long) (getConfig().getDouble("Height") / getConfig().getDouble("DropSpeed")));
    }

    private void removeCrystal(PowerCrystal powerCrystal) {
        occupiedLocations.remove(powerCrystal.getOccupiedLocation());
        powerCrystal.destroy();
    }

    public void stop(){
        powerTimer.cancel();

        activeCrystals.forEach(this::removeCrystal);
        activeCrystals.clear();

        if (powerSpawnTask != null) {
            powerSpawnTask.cancel();
            effectTask.cancel();
            soundTask.cancel();
        }

        this.givenPowerUps.forEach((kit, powerUps) -> powerUps.forEach(kit::removeAttribute));
        this.givenPowerUps.clear();

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getGameManager().isPlayerAlive(player) || player.getGameMode() != GameMode.SURVIVAL) return;

        Kit kit = plugin.getKitManager().getSelectedKit(player);
        OptionalInt openSlot = kit.findOpenSlot();

        if (openSlot.isEmpty()) return;

        List<PowerCrystal> toRemove = new ArrayList<>();

        for (PowerCrystal crystal : activeCrystals) {

            if (crystal.distanceSquared(player) <= getConfig().getDouble("DetectionRadiusSquared")) {
                removeCrystal(crystal);
                toRemove.add(crystal);

                PowerInfo info = crystal.getPowerInfo();
                Ability power = Reflector.newInstance(crystal.getPower(), plugin, info.getConfig(), kit);
                kit.addAbility(power, openSlot.getAsInt());
                this.givenPowerUps.putIfAbsent(kit, new ArrayList<>());
                this.givenPowerUps.get(kit).add(power);

                String playerName = kit.getColor() + player.getName();
                Chat.POWER.broadcast(String.format("%s &7collected the %s&7!", playerName, info.getName()));

                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 2, 1);
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.WOLF_HOWL, 2, 1));

                break;
            }
        }

        toRemove.forEach(activeCrystals::remove);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (activeCrystals.stream().anyMatch(crystal -> crystal.getEntity() == event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
