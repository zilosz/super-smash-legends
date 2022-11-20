package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.HitBoxSelector;
import io.github.aura6.supersmashlegends.utils.finder.range.RangeSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Optional;

public class HerculeanSmash extends RightClickAbility {
    private HerculeanSmashState state = HerculeanSmashState.INACTIVE;
    private LivingEntity entityBeingGrasped;
    private BukkitTask task;
    private double diveHeight;

    public HerculeanSmash(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        switch (state) {

            case INACTIVE:
                sendUseMessage();
                state = HerculeanSmashState.RISING;
                entityBeingGrasped = null;
                player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_IDLE, 2, 1);
                player.setVelocity(new Vector(0, config.getDouble("RiseVelocity"), 0));
                task = Bukkit.getScheduler().runTaskTimer(plugin, this::loop, 4, 0);
                return;

            case RISING:
                state = HerculeanSmashState.DIVING;
                diveHeight = player.getLocation().getY();
                player.setVelocity(player.getEyeLocation().getDirection().multiply(config.getDouble("Dive.Velocity")));
                player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_PIG_ANGRY, 1, 1);
        }
    }

    private void loop() {

        if (EntityUtils.isPlayerGrounded(player)) {
            startCooldown();
            state = HerculeanSmashState.INACTIVE;
            task.cancel();

            if (entityBeingGrasped == null) {
                player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 2);

            } else {
                smash();
            }

        } else {
            RangeSelector selector = new HitBoxSelector(config.getDouble("HitBox"));
            Optional<LivingEntity> target = new EntityFinder(plugin, selector).findClosest(player);

            if (state == HerculeanSmashState.RISING) {

                if (entityBeingGrasped == null) {
                    target.ifPresent(this::grab);
                }

            } else if (entityBeingGrasped == null) {
                target.ifPresent(this::collide);
            }
        }
    }

    private void grab(LivingEntity target) {
        entityBeingGrasped = target;
        player.setPassenger(target);

        player.setVelocity(player.getVelocity().normalize().multiply(config.getDouble("VelocityImpactReduction")));

        Damage damage = Damage.Builder.fromConfig(config.getSection("Grab"), null).build();
        plugin.getDamageManager().attemptAttributeDamage(new AttributeDamageEvent(target, damage, this));

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!target.isValid() || state == HerculeanSmashState.INACTIVE) {
                    entityBeingGrasped = null;
                    cancel();

                } else {
                    player.getWorld().playSound(player.getLocation(), Sound.FUSE, 1, 1);
                }
            }

        }.runTaskTimer(plugin, 0, 0);
    }

    private void collide(LivingEntity target) {
        Bukkit.broadcastMessage(target.getName());

        Damage recoil = Damage.Builder.fromConfig(config.getSection("Recoil"), player.getVelocity()).build();
        plugin.getDamageManager().attemptAttributeDamage(new AttributeDamageEvent(player, recoil, this));

        Damage damage = Damage.Builder.fromConfig(config.getSection("Collide"), player.getVelocity()).build();
        plugin.getDamageManager().attemptAttributeDamage(new AttributeDamageEvent(target, damage, this));

        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_HIT, 2, 0.85f);
    }

    private void smash() {
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 1, 0.5f);
        new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(entityBeingGrasped.getLocation());

        player.eject();

        double distanceDived = diveHeight - player.getLocation().getY();
        double damage;

        if (state == HerculeanSmashState.RISING) {
            damage = config.getDouble("Dive.WeakDamage");

        } else {
            damage = Math.max(0, YamlReader.incLin(config.getSection("Dive"), "Damage", distanceDived, config.getDouble("Dive.DistanceAtMax")));
        }

        double kb = YamlReader.incLin(config.getSection("Dive"), "Kb", distanceDived, config.getDouble("Dive.DistanceAtMax"));

        Damage smash = Damage.Builder.fromConfig(config.getSection("Smash"), player.getVelocity())
                .setDamage(damage).setKb(kb).build();

        plugin.getDamageManager().attemptAttributeDamage(new AttributeDamageEvent(entityBeingGrasped, smash, this));
    }

    @Override
    public void destroy() {
        super.destroy();

        state = HerculeanSmashState.INACTIVE;

        if (entityBeingGrasped != null) {
            entityBeingGrasped = null;
            player.eject();
        }

        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() == player && event.getEntity() == entityBeingGrasped) {
            event.setCancelled(true);
        }
    }

    private enum HerculeanSmashState {
        INACTIVE,
        RISING,
        DIVING
    }
}
