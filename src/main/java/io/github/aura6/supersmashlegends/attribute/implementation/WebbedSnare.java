package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.projectile.ProjectileRemoveReason;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class WebbedSnare extends RightClickAbility {
    private Set<LivingEntity> hitEntities;

    public WebbedSnare(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void launch(boolean first) {
        Location location = EntityUtils.center(this.player);
        location.setDirection(this.player.getEyeLocation().getDirection().multiply(-1));

        SnareProjectile projectile = new SnareProjectile(this.plugin, this, this.config.getSection("Projectile"));
        projectile.setOverrideLocation(location);

        if (first) {
            projectile.setSpread(0);
        }

        projectile.launch();
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPIDER_DEATH, 2, 2);
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(this.config.getDouble("Velocity")));

        this.hitEntities = new HashSet<>();

        this.launch(true);

        for (int i = 1; i < this.config.getInt("WebCount"); i++) {
            this.launch(false);
        }
    }

    @EventHandler
    public void onHitEntity(AttributeDamageEvent event) {
        if (event.getAttribute() != this) return;

        if (this.hitEntities.contains(event.getVictim())) {
            event.setCancelled(true);
        }

        this.hitEntities.add(event.getVictim());
    }

    private static class SnareProjectile extends ItemProjectile {
        private Block webBlock;

        public SnareProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {

            if (this.entity.getLocation().getBlock().getType() == Material.WEB) {
                this.remove(ProjectileRemoveReason.CUSTOM);

            } else if (this.ticksAlive % 2 == 0) {
                new ParticleBuilder(EnumParticle.SNOWBALL).show(this.entity.getLocation());
            }
        }

        private void turnIntoWeb() {
            this.webBlock = this.entity.getLocation().getBlock();
            this.webBlock.setType(Material.WEB);
            int duration = this.config.getInt("WebDuration");

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                if (this.webBlock.getType() == Material.WEB) {
                    this.webBlock.setType(Material.AIR);
                }
            }, duration);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.turnIntoWeb();
            target.setVelocity(new Vector(0, 0, 0));
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.turnIntoWeb();
        }
    }
}
