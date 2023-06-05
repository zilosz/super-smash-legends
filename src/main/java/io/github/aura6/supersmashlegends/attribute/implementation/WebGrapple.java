package io.github.aura6.supersmashlegends.attribute.implementation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.event.DamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class WebGrapple extends RightClickAbility {
    private PacketAdapter packetAdapter;

    public WebGrapple(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new GrappleProjectile(this.plugin, this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.MAGMACUBE_JUMP, 1, 1);
    }

    @Override
    public void activate() {
        super.activate();

        this.packetAdapter = new PacketAdapter(this.plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT) {

            @Override
            public void onPacketSending(PacketEvent event) {

                if (event.getPacket().getType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                    String name = event.getPacket().getStrings().read(0);

                    if (name.equals("mob.bat.idle") && event.getPlayer() == player) {
                        event.setCancelled(true);
                    }
                }
            }

        };

        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetAdapter);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.packetAdapter != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(this.packetAdapter);
        }
    }

    private static class GrappleProjectile extends ItemProjectile {
        private Bat bat;

        public GrappleProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public Item createEntity(Location location) {
            Item item = super.createEntity(location);
            this.bat = location.getWorld().spawn(this.launcher.getEyeLocation(), Bat.class);
            this.bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10_000, 1));
            this.bat.setLeashHolder(item);
            this.launcher.setPassenger(this.bat);
            return item;
        }

        private void pull() {
            Vector direction = VectorUtils.fromTo(this.launcher, this.entity).normalize();
            Vector extraY = new Vector(0, this.config.getDouble("ExtraY"), 0);
            this.launcher.setVelocity(direction.multiply(this.config.getDouble("PullSpeed")).add(extraY));
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.pull();
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.pull();
            this.launcher.playSound(this.launcher.getLocation(), Sound.DIG_WOOD, 1, 1);
        }

        @Override
        public void onRemove() {
            this.bat.remove();
        }

        @EventHandler
        public void onRegularBatDamage(EntityDamageEvent event) {
            if (event.getEntity() == this.bat) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onCustomBatDamage(DamageEvent event) {
            if (event.getVictim() == this.bat) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onDeath(EntityDeathEvent event) {
            if (event.getEntity() == this.bat) {
                this.remove();

            }
        }
    }
}
