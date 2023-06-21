package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.EmulatedProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.EulerAngle;

public class HatThrow extends RightClickAbility {
    private HatProjectile hatProjectile;

    public HatThrow(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (this.hatProjectile == null || this.hatProjectile.state == HatThrowState.INACTIVE) {
            this.hatProjectile = new HatProjectile(this.plugin, this, this.config);
            this.hatProjectile.setLifespan(this.config.getInt("TicksToReturn") + this.config.getInt("ExtraLifespan"));
            this.hatProjectile.launch();

        } else if (this.hatProjectile.state == HatThrowState.DISMOUNTED) {
            this.hatProjectile.mount(this.player);

        } else {
            this.hatProjectile.dismount();
        }
    }

    public enum HatThrowState {
        INACTIVE,
        DISMOUNTED,
        MOUNTED
    }

    public static final class HatProjectile extends EmulatedProjectile<ArmorStand> {
        private HatThrowState state = HatThrowState.INACTIVE;

        public HatProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public ArmorStand createEntity(Location location) {
            ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
            stand.setArms(true);
            stand.setCanPickupItems(false);
            stand.setItemInHand(YamlReader.stack(this.config.getSection("HatItem")));
            stand.setMarker(true);
            stand.setVisible(false);
            return stand;
        }

        @Override
        public void onLaunch() {
            this.state = HatThrowState.DISMOUNTED;
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.ZOMBIE_WOODBREAK, 2, 2);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            this.state = HatThrowState.INACTIVE;

            if (this.ability.isEnabled()) {
                ((ClickableAbility) this.ability).startCooldown();
            }
        }

        @Override
        public void onTick() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.ITEM_PICKUP, 1, 1);
            this.entity.setVelocity(this.launchVelocity.clone().multiply(this.speedFunction(this.ticksAlive)));

            double height = this.ticksAlive * this.config.getDouble("RotationPerTick");
            this.entity.setRightArmPose(new EulerAngle(0, height, 0));
        }

        private double speedFunction(int ticks) {
            return -2 * ticks * this.launchSpeed / this.config.getInt("TicksToReturn") + this.launchSpeed;
        }

        public void mount(Entity passenger) {
            this.state = HatThrowState.MOUNTED;
            this.entity.setPassenger(passenger);
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.CLICK, 2, 1);
        }

        public void dismount() {
            this.state = HatThrowState.DISMOUNTED;
            this.entity.eject();
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.CLICK, 2, 1);
        }
    }
}
