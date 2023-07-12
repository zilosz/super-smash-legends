package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.projectile.ProjectileRemoveReason;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class BombOmb extends RightClickAbility {
    private BombProjectile bombProjectile;

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (this.bombProjectile == null || this.bombProjectile.state == State.INACTIVE) {
            this.sendUseMessage();

            AttackInfo attackInfo = new AttackInfo(AttackType.BOMB_OMB_DIRECT, this);
            this.bombProjectile = new BombProjectile(this.config.getSection("Projectile"), attackInfo);
            this.bombProjectile.launch();

        } else if (this.bombProjectile.state == State.THROWN) {
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

    public enum State {
        INACTIVE,
        THROWN,
        WAITING
    }

    private static class BombProjectile extends ItemProjectile {
        private State state = State.INACTIVE;
        private Block bombBlock;
        private BukkitTask soundTask;
        private BukkitTask explodeTask;
        private boolean hitTarget = false;
        private boolean canExplode = false;

        public BombProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onLaunch() {
            this.state = State.THROWN;
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.ITEM_PICKUP, 2, 0.5f);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            if (!this.hitTarget) {
                this.solidify();
            }
        }

        @Override
        public void onTick() {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
            new ParticleMaker(particle).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.hitTarget = true;
            this.solidify();
        }

        private void solidify() {
            this.remove(ProjectileRemoveReason.CUSTOM);

            this.state = State.WAITING;

            this.bombBlock = this.entity.getLocation().getBlock();
            this.bombBlock.setType(Material.COAL_BLOCK);

            this.soundTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
                this.entity.getWorld().playSound(this.bombBlock.getLocation(), Sound.FUSE, 1, 1);
            }, 0, 0);

            this.explodeTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
                if (this.state == State.WAITING) {
                    this.explode();
                }
            }, this.config.getInt("Explode.Delay"));

            int disableTicks = this.config.getInt("Explode.DisableTicks");
            Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> this.canExplode = true, disableTicks);
        }

        private void explode() {
            this.state = State.INACTIVE;
            this.bombBlock.setType(Material.AIR);

            this.soundTask.cancel();
            this.bombBlock.getWorld().playSound(this.bombBlock.getLocation(), Sound.EXPLODE, 2, 1);

            for (int i = 0; i < 3; i++) {
                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE);
                new ParticleMaker(particle).show(this.bombBlock.getLocation());
            }

            new EntityFinder(new DistanceSelector(this.config.getDouble("Explode.Range")))
                    .setTeamPreference(TeamPreference.ANY)
                    .setAvoidsUser(false)
                    .findAll(this.launcher, this.bombBlock.getLocation())
                    .forEach(this::attemptExplodeHit);

            ((BombOmb) this.attackInfo.getAttribute()).startCooldown();
        }

        private void attemptExplodeHit(LivingEntity target) {
            Section explode = this.config.getSection("Explode");

            double max = explode.getDouble("Range") * explode.getDouble("Range");
            double distanceSq = this.bombBlock.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.decreasingValue(explode, "Damage", distanceSq, max);
            double kb = YamlReader.decreasingValue(explode, "Kb", distanceSq, max);

            Vector direction = VectorUtils.fromTo(this.bombBlock.getLocation(), target.getLocation());

            String name = ((BombOmb) this.attackInfo.getAttribute()).getDisplayName();
            Attack attack = YamlReader.attack(explode, direction, name);
            attack.getDamage().setDamage(damage);
            attack.getKb().setKb(kb);

            AttackInfo attackInfo = new AttackInfo(AttackType.BOMB_OMB_EXPLOSION, this.attackInfo.getAttribute());
            SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
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
