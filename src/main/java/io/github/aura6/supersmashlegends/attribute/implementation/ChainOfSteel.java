package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import io.github.aura6.supersmashlegends.utils.entity.FloatingEntity;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.range.RangeSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ChainOfSteel extends RightClickAbility {
    private Vector direction;

    private BukkitTask chainTask;
    private int chainTicks = 0;

    private final List<FloatingEntity> entities = new ArrayList<>();

    private BukkitTask pullTask;
    private BukkitTask pullSoundTask;
    private BukkitTask effectRemoveTask;

    public ChainOfSteel(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.chainTicks > 0;
    }

    private void reset() {
        if (this.chainTask == null) return;

        this.chainTicks = 0;
        this.chainTask.cancel();

        this.entities.forEach(FloatingEntity::destroy);
        this.entities.clear();

        if (this.pullTask != null) {
            this.pullTask.cancel();
            this.pullSoundTask.cancel();
            this.effectRemoveTask.cancel();
        }
    }

    private void pullTowardsLocation(Location location) {
        this.chainTask.cancel();

        this.player.getWorld().playSound(location, Sound.IRONGOLEM_HIT, 1, 0.5f);

        Vector pullVelocity = this.direction.clone().multiply(this.config.getDouble("PullSpeed"));
        this.pullTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.player.setVelocity(pullVelocity), 0, 0);

        this.pullSoundTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () ->
                this.player.getWorld().playSound(this.player.getLocation(), Sound.STEP_LADDER, 2, 1), 0, 0);

        double distance = location.distance(this.player.getLocation());
        double travelTicks = distance / this.config.getInt("PullSpeed");
        int ticksPerRemoval = (int) (travelTicks / this.entities.size());

        this.effectRemoveTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

            if (this.entities.isEmpty()) {
                this.reset();
                this.startCooldown();

            } else {
                this.entities.remove(this.entities.size() - 1).destroy();
            }
        }, ticksPerRemoval, ticksPerRemoval);
    }



    @Override
    public void onClick(PlayerInteractEvent event) {
        Location currLocation = EntityUtils.center(this.player);
        this.direction = this.player.getEyeLocation().getDirection();
        Vector step = direction.clone().multiply(this.config.getDouble("ChainSpeed"));

        this.chainTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            this.player.setVelocity(new Vector(0, 0.05, 0));
            currLocation.add(step);

            if (this.chainTicks++ >= this.config.getInt("ChainTicks")) {
                this.reset();
                this.startCooldown();
                return;
            }

            if (this.chainTicks % 2 == 0) {
                FloatingEntity entity = new FloatingEntity() {

                    @Override
                    public Entity createEntity(Location location) {
                        FallingBlock entity = location.getWorld().spawnFallingBlock(location, Material.IRON_FENCE, (byte) 0);
                        entity.setHurtEntities(false);
                        entity.setDropItem(false);
                        return entity;
                    }
                };

                entity.spawn(currLocation);
                this.entities.add(entity);
            }

            this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1, 2);

            if (currLocation.getBlock().getType().isSolid()) {
                this.pullTowardsLocation(currLocation);

            } else {
                RangeSelector selector = new HitBoxSelector(this.config.getDouble("HitBox"));

                new EntityFinder(this.plugin, selector).findClosest(this.player, currLocation).ifPresent(target -> {
                    Damage damageObj = Damage.Builder.fromConfig(this.config).build();

                    if (this.plugin.getDamageManager().attemptAttributeDamage(target, damageObj, this)) {
                        this.player.playSound(this.player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                        this.player.getWorld().playSound(currLocation, Sound.EXPLODE, 1, 1.5f);
                        new ParticleBuilder(EnumParticle.EXPLOSION_NORMAL).show(currLocation);
                        this.pullTowardsLocation(currLocation);
                    }
                });
            }
        }, 0, 0);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() == this.player) {
            this.reset();
            this.startCooldown();
            this.player.playSound(this.player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 2);
        }
    }
}
