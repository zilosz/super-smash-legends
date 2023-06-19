package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.ClickableAbility;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.file.YamlReader;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class BombOmb extends RightClickAbility {
    private BombProjectile bombProjectile;

    public BombOmb(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (this.bombProjectile == null || this.bombProjectile.state == BombState.INACTIVE) {
            sendUseMessage();

            this.bombProjectile = new BombProjectile(this.plugin, this, this.config.getSection("Projectile"));
            this.bombProjectile.launch();

        } else if (this.bombProjectile.state == BombState.THROWN) {
            this.bombProjectile.solidify();

        } else if (this.bombProjectile.canExplode) {
            this.bombProjectile.explode();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.bombProjectile != null) {
            this.bombProjectile.destroy();
        }
    }

    public enum BombState {
        INACTIVE,
        THROWN,
        WAITING
    }

    public static class BombProjectile extends ItemProjectile {
        private BombState state = BombState.INACTIVE;
        private Block bombBlock;
        private BukkitTask soundTask;
        private BukkitTask explodeTask;
        private boolean hitTarget = false;
        private boolean canExplode = false;

        public BombProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            this.state = BombState.THROWN;
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.ITEM_PICKUP, 2, 0.5f);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).show(this.entity.getLocation());
        }

        private void attemptExplodeHit(LivingEntity target) {
            Section explode = this.config.getSection("Explode");

            double max = explode.getDouble("Range") * explode.getDouble("Range");
            double distanceSq = this.bombBlock.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.decLin(explode, "Damage", distanceSq, max);
            double kb = YamlReader.decLin(explode, "Kb", distanceSq, max);

            Vector direction = VectorUtils.fromTo(this.bombBlock.getLocation(), target.getLocation());

            this.plugin.getDamageManager().attack(target, this.ability, new AttackSettings(explode, direction)
                    .modifyDamage(settings -> settings.setDamage(damage))
                    .modifyKb(settings -> settings.setKb(kb)));
        }

        private void explode() {
            this.state = BombState.INACTIVE;
            this.bombBlock.setType(Material.AIR);

            this.soundTask.cancel();
            this.bombBlock.getWorld().playSound(this.bombBlock.getLocation(), Sound.EXPLODE, 2, 1);

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.bombBlock.getLocation());
            }

            new EntityFinder(this.plugin, new DistanceSelector(this.config.getDouble("Explode.Range")))
                    .setTeamPreference(TeamPreference.ANY)
                    .setAvoidsUser(false)
                    .findAll(this.launcher, this.bombBlock.getLocation())
                    .forEach(this::attemptExplodeHit);

            if (this.ability instanceof ClickableAbility) {
                ((ClickableAbility) this.ability).startCooldown();
            }
        }

        private void solidify() {
            this.remove(ProjectileRemoveReason.CUSTOM);

            this.state = BombState.WAITING;

            this.bombBlock = this.entity.getLocation().getBlock();
            this.bombBlock.setType(Material.COAL_BLOCK);

            this.soundTask = Bukkit.getScheduler().runTaskTimer(this.plugin,
                    () -> this.entity.getWorld().playSound(this.bombBlock.getLocation(), Sound.FUSE, 1, 1), 0, 0);

            this.explodeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (this.state == BombState.WAITING) {
                    this.explode();
                }
            }, this.config.getInt("Explode.Delay"));

            int disableTicks = this.config.getInt("Explode.DisableTicks");
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.canExplode = true, disableTicks);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            if (!this.hitTarget) {
                this.solidify();
            }
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.hitTarget = true;
            this.solidify();
        }

        private void destroy() {
            this.remove(ProjectileRemoveReason.DEACTIVATION);

            if (this.explodeTask != null) {
                this.explodeTask.cancel();
                this.soundTask.cancel();
                this.bombBlock.setType(Material.AIR);
            }
        }
    }
}
