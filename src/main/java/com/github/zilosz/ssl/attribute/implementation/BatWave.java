package com.github.zilosz.ssl.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class BatWave extends RightClickAbility {
    private final Set<BatProjectile> batProjectiles = new HashSet<>();
    private BatWaveState state = BatWaveState.INACTIVE;
    private BukkitTask resetTask;

    public BatWave(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void addAndLaunch(BatProjectile projectile, Location location) {
        this.batProjectiles.add(projectile);
        projectile.setOverrideLocation(location);
        projectile.launch();
    }

    private void onInactiveClick() {
        this.sendUseMessage();

        Location center = this.player.getEyeLocation();
        Vector alt = this.player.getLocation().getDirection();

        double width = this.config.getDouble("Width");
        double height = this.config.getDouble("Height");
        int count = this.config.getInt("BatCount");

        Set<Location> locations = VectorUtils.getRectLocations(center, alt, width, height, count);
        locations.forEach(loc -> this.addAndLaunch(new BatProjectile(this.plugin, this, this.config), loc));

        this.resetTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.reset();
            this.startCooldown();
        }, this.config.getInt("Lifespan"));
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (this.state) {

            case INACTIVE:
                this.onInactiveClick();
                this.state = BatWaveState.UNLEASHED;
                break;

            case UNLEASHED:
                this.batProjectiles.forEach(BatProjectile::leash);
                this.state = BatWaveState.LEASHED;
                break;

            case LEASHED:
                this.batProjectiles.forEach(BatProjectile::unleash);
                this.state = BatWaveState.UNLEASHED;
        }
    }

    @Override
    public void run() {
        super.run();

        if (this.state == BatWaveState.LEASHED) {
            this.player.setVelocity(this.batProjectiles.iterator().next().getLaunchVelocity());
        }
    }

    private void reset() {
        this.state = BatWaveState.INACTIVE;
        this.batProjectiles.forEach(projectile -> projectile.remove(ProjectileRemoveReason.DEACTIVATION));
        this.batProjectiles.clear();

        if (this.resetTask != null) {
            this.resetTask.cancel();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    private enum BatWaveState {
        INACTIVE,
        LEASHED,
        UNLEASHED
    }

    private static class BatProjectile extends LivingProjectile<ArmorStand> {
        private Bat bat;

        public BatProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public ArmorStand createEntity(Location location) {
            ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
            stand.setVisible(false);
            stand.setMarker(true);

            this.bat = location.getWorld().spawn(location, Bat.class);
            stand.setPassenger(this.bat);

            return stand;
        }

        @Override
        public void onLaunch() {
            this.plugin.getTeamManager().getPlayerTeam(this.ability.getPlayer()).addEntity(this.bat);
        }

        public void leash() {
            this.bat.setLeashHolder(this.launcher);
        }

        public void unleash() {
            this.bat.setLeashHolder(null);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            super.onRemove(reason);
            this.unleash();
            this.bat.remove();
            this.plugin.getTeamManager().getPlayerTeam(this.ability.getPlayer()).removeEntity(this.bat);
        }

        @EventHandler
        public void onDamageBat(DamageEvent event) {
            if (event.getVictim() == this.bat) {
                event.setCancelled(true);
            }
        }
    }
}
