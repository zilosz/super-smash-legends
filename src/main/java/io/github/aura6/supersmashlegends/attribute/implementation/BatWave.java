package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.LivingProjectile;
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
    private MainBatProjectile mainBatProjectile;
    private Set<BatProjectile> batProjectiles;

    public BatWave(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void addAndLaunch(BatProjectile projectile, Location location) {
        this.batProjectiles.add(projectile);
        projectile.setOverrideLocation(location);
        projectile.launch();
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (this.mainBatProjectile == null || this.mainBatProjectile.state == BatWaveState.INACTIVE) {
            this.sendUseMessage();
            this.batProjectiles = new HashSet<>();

            Location center = this.player.getEyeLocation();
            Vector alt = this.player.getLocation().getDirection();
            double width = this.config.getDouble("Width");
            double height = this.config.getDouble("Height");
            int count = this.config.getInt("BatCount");
            Set<Location> locations = VectorUtils.getRectSpread(center, alt, width, height, count);

            this.mainBatProjectile = new MainBatProjectile(this.plugin, this, this.config);
            Location first = locations.iterator().next();
            this.addAndLaunch(mainBatProjectile, first);
            locations.remove(first);

            locations.forEach(loc -> this.addAndLaunch(new BatProjectile(this.plugin, this, this.config), loc));

        } else if (this.mainBatProjectile.state == BatWaveState.UNLEASHED) {
            this.batProjectiles.forEach(BatProjectile::leash);

        } else {
            this.batProjectiles.forEach(BatProjectile::unleash);
        }
    }

    public enum BatWaveState {
        INACTIVE,
        LEASHED,
        UNLEASHED
    }

    public static class BatProjectile extends LivingProjectile<ArmorStand> {
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
        public void onRemove() {
            super.onRemove();
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

    public static class MainBatProjectile extends BatProjectile {
        private BatWaveState state = BatWaveState.INACTIVE;

        public MainBatProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            super.onLaunch();
            this.state = BatWaveState.UNLEASHED;
        }

        @Override
        public void leash() {
            super.leash();
            this.state = BatWaveState.LEASHED;
        }

        @Override
        public void unleash() {
            super.unleash();
            this.state = BatWaveState.UNLEASHED;
        }

        @Override
        public void onTick() {
            if (this.state == BatWaveState.LEASHED) {
                this.launcher.setVelocity(this.constantVelocity);
            }
        }

        @Override
        public void onRemove() {
            super.onRemove();

            this.state = BatWaveState.INACTIVE;

            if (this.ability.isEnabled()) {
                ((ClickableAbility) this.ability).startCooldown();
            }
        }
    }
}
