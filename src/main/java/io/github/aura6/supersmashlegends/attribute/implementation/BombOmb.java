package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import io.github.aura6.supersmashlegends.utils.finder.range.DistanceSelector;
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

        if (bombProjectile == null || bombProjectile.state == BombState.INACTIVE) {
            sendUseMessage();

            bombProjectile = new BombProjectile(plugin, this, config.getSection("Projectile"));
            bombProjectile.launch();

        } else if (bombProjectile.state == BombState.THROWN) {
            bombProjectile.solidify();

        } else {
            bombProjectile.explode();
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
            this.state = BombState.WAITING;

            this.bombBlock = this.entity.getLocation().getBlock();
            this.bombBlock.setType(Material.COAL_BLOCK);

            remove();

            soundTask = Bukkit.getScheduler().runTaskTimer(plugin,
                    () -> this.entity.getWorld().playSound(this.bombBlock.getLocation(), Sound.FUSE, 1, 1), 0, 0);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (this.state == BombState.WAITING) {
                    explode();
                }
            }, config.getInt("Explode.Delay"));
        }

        private void explode() {
            this.state = BombState.INACTIVE;
            this.bombBlock.setType(Material.AIR);

            soundTask.cancel();
            this.bombBlock.getWorld().playSound(this.bombBlock.getLocation(), Sound.EXPLODE, 2, 1);

            if (this.ability instanceof ClickableAbility) {
                ((ClickableAbility) this.ability).startCooldown();
            }

            for (int i = 0; i < 3; i++) {
                new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.bombBlock.getLocation());
            }

            new EntityFinder(this.plugin, new DistanceSelector(config.getDouble("Explode.Range")))
                    .setTeamPreference(TeamPreference.ANY)
                    .setAvoidsUser(false)
                    .findAll(this.launcher, this.bombBlock.getLocation())
                    .forEach(this::attemptExplodeHit);
        }

        private void attemptExplodeHit(LivingEntity target) {
            Section explode = config.getSection("Explode");

            double max = explode.getDouble("Range") * explode.getDouble("Range");
            double distanceSq = this.bombBlock.getLocation().distanceSquared(target.getLocation());
            double damage = YamlReader.decLin(explode, "Damage", distanceSq, max);
            double kb = YamlReader.decLin(explode, "Kb", distanceSq, max);

            Vector direction = VectorUtils.fromTo(this.bombBlock.getLocation(), target.getLocation());
            Damage dmg = Damage.Builder.fromConfig(explode, direction).setDamage(damage).setKb(kb).build();

            plugin.getDamageManager().attemptAttributeDamage(new AttributeDamageEvent(target, dmg, this.ability));
        }
    }
}
