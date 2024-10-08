package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashSet;
import java.util.Set;

public class WebbedSnare extends RightClickAbility {
    private Set<LivingEntity> hitEntities;

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPIDER_DEATH, 2, 2);

        double velocity = this.config.getDouble("Velocity");
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(velocity));

        this.hitEntities = new HashSet<>();

        Vector direction = this.player.getEyeLocation().getDirection().multiply(-1);
        Location source = EntityUtils.center(this.player).setDirection(direction);

        this.launch(source, true);

        double angle = this.config.getDouble("ConicAngle");
        int count = this.config.getInt("ExtraWebCount");

        for (Vector vector : VectorUtils.conicVectors(source, angle, count)) {
            this.launch(source.setDirection(vector), false);
        }
    }

    private void launch(Location source, boolean first) {
        AttackInfo attackInfo = new AttackInfo(AttackType.WEBBED_SNARE, this);
        SnareProjectile projectile = new SnareProjectile(this.config.getSection("Projectile"), attackInfo);
        projectile.setOverrideLocation(source);

        if (first) {
            projectile.setSpread(0);
        }

        projectile.launch();
    }

    @EventHandler
    public void onHitEntity(AttackEvent event) {
        if (event.getAttackInfo().getAttribute() != this) return;

        if (this.hitEntities.contains(event.getVictim())) {
            event.setCancelled(true);
        }

        this.hitEntities.add(event.getVictim());
    }

    private static class SnareProjectile extends ItemProjectile {
        private Block webBlock;

        public SnareProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.turnIntoWeb(null);
        }

        @Override
        public void onTick() {

            if (this.entity.getLocation().getBlock().getType() == Material.WEB) {
                this.remove(ProjectileRemoveReason.HIT_BLOCK);

            } else if (this.ticksAlive % 2 == 0) {
                new ParticleMaker(new ParticleBuilder(ParticleEffect.SNOWBALL)).show(this.entity.getLocation());
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.turnIntoWeb(target);
            target.setVelocity(new Vector(0, 0, 0));
        }

        private void turnIntoWeb(LivingEntity target) {

            if (target == null) {
                this.webBlock = this.entity.getLocation().getBlock();

            } else {
                this.webBlock = target.getLocation().getBlock();
            }

            this.webBlock.setType(Material.WEB);
            int duration = this.config.getInt("WebDuration");

            Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
                if (this.webBlock.getType() == Material.WEB) {
                    this.webBlock.setType(Material.AIR);
                }
            }, duration);
        }
    }
}
