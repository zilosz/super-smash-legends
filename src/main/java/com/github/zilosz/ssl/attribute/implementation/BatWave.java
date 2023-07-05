package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class BatWave extends RightClickAbility {
    private final Set<BatProjectile> bats = new HashSet<>();
    private State state = State.INACTIVE;
    private BukkitTask resetTask;

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (this.state) {

            case INACTIVE:
                this.onInactiveClick();
                this.state = State.UNLEASHED;
                break;

            case UNLEASHED:
                this.bats.forEach(BatProjectile::leash);
                this.state = State.LEASHED;
                break;

            case LEASHED:
                this.bats.forEach(BatProjectile::unleash);
                this.state = State.UNLEASHED;
        }
    }

    private void onInactiveClick() {
        this.sendUseMessage();

        Location center = this.player.getEyeLocation();
        Vector alt = this.player.getLocation().getDirection();

        double width = this.config.getDouble("Width");
        double height = this.config.getDouble("Height");
        int count = this.config.getInt("BatCount");

        Set<Location> locations = VectorUtils.getRectLocations(center, alt, width, height, count);
        locations.forEach(loc -> this.addAndLaunch(new BatProjectile(this, this.config), loc));

        this.resetTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.reset();
            this.startCooldown();
        }, this.config.getInt("Lifespan"));
    }

    private void addAndLaunch(BatProjectile projectile, Location location) {
        this.bats.add(projectile);
        projectile.setOverrideLocation(location);
        projectile.launch();
    }

    private void reset() {
        this.state = State.INACTIVE;
        CollectionUtils.removeWhileIterating(this.bats, bat -> bat.remove(ProjectileRemoveReason.DEACTIVATION));

        if (this.resetTask != null) {
            this.resetTask.cancel();
        }
    }

    @Override
    public void run() {
        super.run();

        if (this.state == State.LEASHED) {
            this.player.setVelocity(this.bats.iterator().next().getLaunchVelocity());
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    private enum State {
        INACTIVE,
        LEASHED,
        UNLEASHED
    }

    private static class BatProjectile extends LivingProjectile<ArmorStand> {
        private Bat bat;

        public BatProjectile(Ability ability, Section config) {
            super(ability, config);
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
        public void onTargetHit(LivingEntity target) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.BAT_DEATH, 1, 1);
        }

        @Override
        public void onLaunch() {
            SSL.getInstance().getTeamManager().getPlayerTeam(this.launcher).addEntity(this.bat);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            super.onRemove(reason);
            this.unleash();
            this.bat.remove();
            SSL.getInstance().getTeamManager().getPlayerTeam(this.launcher).removeEntity(this.bat);
        }

        public void unleash() {
            this.bat.setLeashHolder(null);
        }

        public void leash() {
            this.bat.setLeashHolder(this.launcher);
        }

        @EventHandler
        public void onDamageBat(DamageEvent event) {
            if (event.getVictim() == this.bat) {
                event.setCancelled(true);
            }
        }
    }
}
