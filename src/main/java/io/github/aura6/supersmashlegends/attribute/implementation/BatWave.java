package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.event.projectile.ProjectileRemoveEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.LivingProjectile;
import io.github.aura6.supersmashlegends.projectile.ProjectileRemoveReason;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class BatWave extends RightClickAbility {
    private final Set<BatProjectile> batProjectiles = new HashSet<>();
    private BatWaveState state = BatWaveState.INACTIVE;

    public BatWave(SuperSmashLegends plugin, Section config, Kit kit) {
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

        Set<Location> locations = VectorUtils.getRectSpread(center, alt, width, height, count);
        locations.forEach(loc -> this.addAndLaunch(new BatProjectile(this.plugin, this, this.config), loc));
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (this.state) {

            case INACTIVE:
                this.onInactiveClick();
                this.state = BatWaveState.UNLEASHED;
                System.out.println("inactive");
                break;

            case UNLEASHED:
                this.batProjectiles.forEach(BatProjectile::leash);
                this.state = BatWaveState.LEASHED;
                System.out.println("unleashed");
                break;

            case LEASHED:
                this.batProjectiles.forEach(BatProjectile::unleash);
                this.state = BatWaveState.UNLEASHED;
                System.out.println("leashed");
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
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onProjectileRemove(ProjectileRemoveEvent event) {
        if (!(event.getProjectile() instanceof BatProjectile)) return;

        BatProjectile projectile = (BatProjectile) event.getProjectile();

        if (this.batProjectiles.contains(projectile) && event.getReason() != ProjectileRemoveReason.DEACTIVATION) {
            this.reset();
            this.startCooldown();
        }
    }

    private enum BatWaveState {
        INACTIVE,
        LEASHED,
        UNLEASHED
    }

    private static class BatProjectile extends LivingProjectile<ArmorStand> {
        private Bat bat;

        public BatProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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
        }

        @EventHandler
        public void onCustomDamageBat(DamageEvent event) {
            if (event.getVictim() == this.bat) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onRegularDamageBat(EntityDamageEvent event) {
            if (event.getEntity() == this.bat) {
                event.setCancelled(true);
            }
        }
    }
}
