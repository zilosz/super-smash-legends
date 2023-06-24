package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.damage.DamageSettings;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.projectile.ProjectileLaunchEvent;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.NmsUtils;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShadowCloneJutsu extends RightClickAbility {
    private final List<ShadowClone> clones = new ArrayList<>();

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.BLAZE_HIT, 0.5f, 1);

        Vector direction = this.player.getEyeLocation().getDirection();
        this.player.setVelocity(direction.clone().multiply(-this.config.getDouble("Recoil")));

        Skeleton creature = this.player.getWorld().spawn(this.player.getLocation(), Skeleton.class);
        creature.setSkeletonType(Skeleton.SkeletonType.WITHER);

        int speed = this.config.getInt("Clone.Speed");
        new PotionEffectEvent(creature, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();

        creature.setMaxHealth(this.config.getInt("Clone.Health"));
        creature.getEquipment().setItemInHand(null);
        creature.setCustomName(MessageUtils.color("&8Shadow Clone"));
        creature.setCustomNameVisible(true);

        DisguiseAPI.disguiseEntity(creature, new PlayerDisguise(this.player.getName()));

        ShadowClone clone = new ShadowClone(this, this.config, creature, this.clones);
        SSL.getInstance().getTeamManager().getPlayerTeam(this.player).addEntity(clone.creature);
        this.clones.add(clone);

        if (this.clones.size() > this.config.getInt("Clone.Limit")) {
            this.clones.get(0).destroy();
        }

        clone.creature.setVelocity(direction.multiply(this.config.getDouble("Clone.Velocity")));

        clone.runTaskTimer(SSL.getInstance(), 0, 10);
        Bukkit.getPluginManager().registerEvents(clone, SSL.getInstance());

        Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
            if (clone.creature.isValid()) {
                clone.destroy();
            }
        }, this.config.getInt("Clone.Duration"));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        new ArrayList<>(this.clones).forEach(ShadowClone::destroy);
    }

    private static class ShadowClone extends BukkitRunnable implements Listener {
        private final ShadowCloneJutsu ability;
        private final Section config;
        private final Creature creature;
        private final List<ShadowClone> clones;
        private Location lastShurikenLocation;
        private LivingEntity target;
        private BukkitTask rasenganTask;

        public ShadowClone(ShadowCloneJutsu ability, Section config, Creature creature, List<ShadowClone> friends) {
            this.config = config;
            this.creature = creature;
            this.ability = ability;
            this.clones = friends;
        }

        @EventHandler
        public void onRasenganStart(Rasengan.RasenganStartEvent event) {
            if (event.getRasengan().getPlayer() != this.ability.getPlayer()) return;

            int speed = this.config.getInt("Clone.Speed");
            int rasenganSpeed = this.config.getInt("Clone.Rasengan.Speed");

            Rasengan.RasenganStartEvent.apply(this.creature, speed + rasenganSpeed);
            ShadowClone instance = this;

            this.rasenganTask = new BukkitRunnable() {
                int ticksCharged = 0;

                @Override
                public void run() {

                    if (++this.ticksCharged >= ShadowClone.this.config.getInt("Clone.Rasengan.Lifespan")) {
                        ShadowClone.this.endRasengan();
                        return;
                    }

                    Rasengan.display(instance.creature);
                }

            }.runTaskTimer(SSL.getInstance(), 0, 0);
        }

        private void endRasengan() {
            this.rasenganTask.cancel();
            Rasengan.end(this.creature);

            int speed = this.config.getInt("Clone.Speed");
            new PotionEffectEvent(this.creature, PotionEffectType.SPEED, Integer.MAX_VALUE, speed).apply();
        }

        @EventHandler
        public void onRasenshurikenDisplay(Rasenshuriken.RasenshurikenDisplayEvent event) {
            if (event.getRasenshuriken().getPlayer() != this.ability.getPlayer()) return;

            double height = this.config.getDouble("Clone.Rasenshuriken.Height");
            this.lastShurikenLocation = EntityUtils.top(this.creature).add(0, height, 0);
            Rasenshuriken.display(this.lastShurikenLocation, false, this.config);
        }

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            if (event.getEntity() == this.creature) {
                this.destroy();
            }
        }

        private void destroy() {
            this.cancel();

            this.clones.remove(this);
            this.creature.remove();

            HandlerList.unregisterAll(this);
            Optional.ofNullable(this.rasenganTask).ifPresent(BukkitTask::cancel);

            this.creature.getWorld().playSound(this.creature.getLocation(), Sound.FIRE, 1, 1);
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(this.creature.getLocation(), 1.5, 10, 0.1);
            this.ability.getPlayer().playSound(this.ability.getPlayer().getLocation(), Sound.BLAZE_HIT, 2, 1);

            SSL.getInstance().getTeamManager().getPlayerTeam(this.ability.getPlayer()).removeEntity(this.creature);
        }

        @EventHandler
        public void onProjectileLaunch(ProjectileLaunchEvent event) {
            if (!(event.getProjectile().getAbility() instanceof Rasenshuriken)) return;
            if (event.getProjectile().getLauncher() != this.ability.getPlayer()) return;

            Vector direction;

            if (this.target == null) {
                direction = this.ability.getPlayer().getEyeLocation().getDirection();

            } else {
                direction = VectorUtils.fromTo(this.lastShurikenLocation, this.target.getLocation());
            }

            Rasenshuriken.Shuriken shuriken = (Rasenshuriken.Shuriken) event.getProjectile().copy(this.ability);
            shuriken.setOverrideLocation(this.lastShurikenLocation.setDirection(direction));

            DamageSettings damageSettings = shuriken.getAttackSettings().getDamageSettings();
            double multiplier = this.config.getDouble("Clone.Rasenshuriken.DamageMultiplier");
            damageSettings.setDamage(damageSettings.getDamage() * multiplier);
            shuriken.setSpeed(event.getProjectile().getSpeed());
            shuriken.launch();
        }

        @EventHandler
        public void onCloneMelee(EntityDamageByEntityEvent event) {
            if (event.getDamager() == this.creature) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onTarget(EntityTargetLivingEntityEvent event) {
            if (event.getEntity() == this.creature && event.getTarget() == this.ability.getPlayer()) {
                event.setCancelled(true);
            }
        }

        @Override
        public void run() {
            EntitySelector selector = new DistanceSelector(this.config.getDouble("Clone.Vision"));
            EntityFinder finder = new EntityFinder(selector);

            finder.findClosest(this.ability.getPlayer(), this.creature.getLocation()).ifPresent(target -> {
                this.target = target;
                this.creature.setTarget(target);
            });
        }

        @EventHandler
        public void onArmSwing(PlayerAnimationEvent event) {
            if (event.getPlayer() != this.ability.getPlayer()) return;

            EntityLiving nmsEntity = NmsUtils.getLiving(this.creature);
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(nmsEntity, 0);
            Bukkit.getOnlinePlayers().forEach(player -> NmsUtils.sendPacket(player, packet));

            double stepped = 0;
            boolean found = false;

            Location eye = this.creature.getEyeLocation();
            Location curr = eye.subtract(0, 0.5, 0);
            Vector step = eye.getDirection().multiply(0.1);

            Team team = SSL.getInstance().getTeamManager().getPlayerTeam(this.ability.getPlayer());

            while (!found && stepped < 3) {
                curr.add(step);
                stepped += 0.1;

                for (LivingEntity target : this.creature.getWorld().getLivingEntities()) {
                    if (TeamPreference.FRIENDLY.validate(team, target)) continue;
                    if (!BlockUtils.isLocationInsideBox(curr, NmsUtils.getLiving(target).getBoundingBox())) continue;

                    AttackSettings settings;

                    if (this.rasenganTask == null) {
                        settings = new AttackSettings(this.config.getSection("Clone.Melee"), step)
                                .modifyDamage(damage -> damage.setDamage(this.ability.getKit().getDamage()));

                    } else {
                        settings = new AttackSettings(this.config.getSection("Clone.Rasengan"), step);

                        Rasengan.displayAttackEffect(this.creature);
                        this.endRasengan();
                    }

                    if (SSL.getInstance().getDamageManager().attack(target, this.ability, settings)) {
                        Location loc = this.ability.getPlayer().getLocation();
                        this.ability.getPlayer().playSound(loc, Sound.ORB_PICKUP, 1, 1);
                    }

                    found = true;
                    break;
                }
            }
        }
    }
}
