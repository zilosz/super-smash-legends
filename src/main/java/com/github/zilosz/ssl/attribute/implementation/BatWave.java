package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.projectile.LivingProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import com.github.zilosz.ssl.util.message.Chat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BatWave extends RightClickAbility {
    private final List<BatProjectile> bats = new ArrayList<>();
    private State state = State.INACTIVE;
    private BukkitTask resetTask;
    private boolean hasSlinged = false;

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (this.state) {

            case INACTIVE:
                this.launch();
                break;

            case UNLEASHED:
                this.leash();
                break;

            case LEASHED:
                this.unleash();
        }
    }

    private void launch() {
        this.state = State.UNLEASHED;
        this.sendUseMessage();

        Location center = this.player.getEyeLocation();

        double width = this.config.getDouble("Width");
        double height = this.config.getDouble("Height");
        int count = this.config.getInt("BatCount");

        List<Location> locations = VectorUtils.flatRectLocations(center, width, height, count, true);
        AttackInfo attackInfo = new AttackInfo(AttackType.BAT_WAVE, this);
        locations.forEach(loc -> this.addAndLaunch(new BatProjectile(this.config, attackInfo), loc));

        this.resetTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            this.reset();
            this.startCooldown();
        }, this.config.getInt("Lifespan"));
    }

    private void leash() {
        this.bats.forEach(BatProjectile::leash);
        this.state = State.LEASHED;
    }

    private void unleash() {
        this.bats.forEach(BatProjectile::unleash);
        this.state = State.UNLEASHED;
    }

    private void addAndLaunch(BatProjectile projectile, Location location) {
        this.bats.add(projectile);
        projectile.setOverrideLocation(location);
        projectile.launch();
    }

    private void reset() {
        this.state = State.INACTIVE;
        this.hasSlinged = false;
        CollectionUtils.removeWhileIterating(this.bats, bat -> bat.remove(ProjectileRemoveReason.DEACTIVATION));

        if (this.resetTask != null) {
            this.resetTask.cancel();
        }
    }

    @Override
    public void run() {
        super.run();

        if (this.state == State.LEASHED) {
            this.player.setVelocity(this.bats.get(0).getLaunchVelocity());
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer() != this.player) return;
        if (this.state != State.LEASHED) return;
        if (this.hasSlinged) return;

        this.hasSlinged = true;

        Vector slingVector = VectorUtils.fromTo(this.player, this.bats.get(0).getEntity());
        slingVector.normalize().multiply(this.config.getDouble("SlingVelocity"));
        this.player.setVelocity(slingVector);

        this.player.getWorld().playSound(this.player.getLocation(), Sound.MAGMACUBE_JUMP, 2, 1);
        Chat.ABILITY.send(this.player, "&7You threw yourself like a slingshot!");

        if (this.state == State.LEASHED) {
            this.unleash();
        }
    }

    private enum State {
        INACTIVE,
        LEASHED,
        UNLEASHED
    }

    private static class BatProjectile extends LivingProjectile<ArmorStand> {
        private Bat bat;

        public BatProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
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
            SSL.getInstance().getTeamManager().addEntityToTeam(this.bat, this.launcher);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            super.onRemove(reason);
            this.unleash();
            this.bat.remove();
            SSL.getInstance().getTeamManager().removeEntityFromTeam(this.bat);
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
