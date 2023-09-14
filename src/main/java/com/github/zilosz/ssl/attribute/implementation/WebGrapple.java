package com.github.zilosz.ssl.attribute.implementation;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.SoundCanceller;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class WebGrapple extends RightClickAbility {
    private SoundCanceller batSoundCanceller;
    private GrappleProjectile grappleProjectile;

    @Override
    public void activate() {
        super.activate();

        this.batSoundCanceller = new SoundCanceller(SSL.getInstance(), "mob.bat.idle");
        ProtocolLibrary.getProtocolManager().addPacketListener(this.batSoundCanceller);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        AttackInfo attackInfo = new AttackInfo(AttackType.WEB_GRAPPLE, this);
        this.grappleProjectile = new GrappleProjectile(this.config.getSection("Projectile"), attackInfo);
        this.grappleProjectile.launch();

        this.player.getWorld().playSound(this.player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 1);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.batSoundCanceller != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(this.batSoundCanceller);
        }

        if (this.grappleProjectile != null) {
            this.grappleProjectile.remove(ProjectileRemoveReason.DEACTIVATION);
        }
    }

    private static class GrappleProjectile extends ItemProjectile {
        private Bat bat;

        public GrappleProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public Item createEntity(Location location) {
            Item item = super.createEntity(location);
            this.bat = location.getWorld().spawn(this.launcher.getEyeLocation(), Bat.class);
            new PotionEffectEvent(this.bat, PotionEffectType.INVISIBILITY, 10_000, 1).apply();
            this.bat.setLeashHolder(item);
            this.launcher.setPassenger(this.bat);
            return item;
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.pull();
            this.launcher.playSound(this.launcher.getLocation(), Sound.DIG_WOOD, 1, 1);
        }

        @Override
        public void onRemove(ProjectileRemoveReason reason) {
            this.bat.remove();
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.pull();
        }

        private void pull() {
            Vector direction = VectorUtils.fromTo(this.launcher, this.entity).normalize();
            Vector extraY = new Vector(0, this.config.getDouble("ExtraY"), 0);
            this.launcher.setVelocity(direction.multiply(this.config.getDouble("PullSpeed")).add(extraY));
        }

        @EventHandler
        public void onBatAttack(AttackEvent event) {
            if (event.getVictim() == this.bat) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onDeath(EntityDeathEvent event) {
            if (event.getEntity() == this.bat) {
                this.remove(ProjectileRemoveReason.ENTITY_DEATH);
            }
        }
    }
}
