package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
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

    public BombOmb(SuperSmashLegends plugin, Section config, Kit kit) {
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

        } else {
            this.bombProjectile.explode();
        }
    }

    public enum BombState {
        INACTIVE,
        THROWN,
        WAITING,
        MAKING_BOMB
    }

    public static class BombProjectile extends ItemProjectile {
        private BombState state = BombState.INACTIVE;
        private Block bombBlock;
        private BukkitTask soundTask;

        public BombProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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

        @Override
        public void onBlockHit(BlockHitResult result) {
            solidify();
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            solidify();
        }

        private void solidify() {
            remove();
            this.state = BombState.MAKING_BOMB;

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                if (this.state != BombState.MAKING_BOMB) return;

                this.state = BombState.WAITING;

                this.bombBlock = this.entity.getLocation().getBlock();
                this.bombBlock.setType(Material.COAL_BLOCK);

                this.soundTask = Bukkit.getScheduler().runTaskTimer(this.plugin,
                        () -> this.entity.getWorld().playSound(this.bombBlock.getLocation(), Sound.FUSE, 1, 1), 0, 0);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (this.state == BombState.WAITING) {
                        explode();
                    }
                }, this.config.getInt("Explode.Delay"));
            }, 1);
        }

        private void explode() {
            if (this.bombBlock == null) return;

            this.state = BombState.INACTIVE;
            this.bombBlock.setType(Material.AIR);

            this.soundTask.cancel();
            this.bombBlock.getWorld().playSound(this.bombBlock.getLocation(), Sound.EXPLODE, 2, 1);

            if (this.ability instanceof ClickableAbility) {
                ((ClickableAbility) this.ability).startCooldown();
            }

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.bombBlock.getLocation());
            }

            new EntityFinder(this.plugin, new DistanceSelector(this.config.getDouble("Explode.Range")))
                    .setTeamPreference(TeamPreference.ANY)
                    .setAvoidsUser(false)
                    .findAll(this.launcher, this.bombBlock.getLocation())
                    .forEach(this::attemptExplodeHit);
        }

        private void attemptExplodeHit(LivingEntity target) {
            Section explode = this.config.getSection("Explode");

            double max = explode.getDouble("Range") * explode.getDouble("Range");
            double distanceSq = this.bombBlock.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.decLin(explode, "Damage", distanceSq, max);
            double kb = YamlReader.decLin(explode, "Kb", distanceSq, max);

            Vector direction = VectorUtils.fromTo(this.bombBlock.getLocation(), target.getLocation());
            Damage dmg = Damage.Builder.fromConfig(explode, direction).setDamage(damage).setKb(kb).build();

            plugin.getDamageManager().attemptAttributeDamage(target, dmg, this.ability);
        }
    }
}
