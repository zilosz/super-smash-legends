package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.event.ProjectileLaunchEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.Team;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.NmsUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
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
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShadowCloneJutsu extends RightClickAbility {
    private final List<ShadowClone> clones = new ArrayList<>();

    public ShadowCloneJutsu(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getLocation(), Sound.WITHER_IDLE, 1, 1);

        Vector direction = player.getEyeLocation().getDirection();
        player.setVelocity(direction.clone().multiply(-config.getDouble("Recoil")));
        kit.getJump().giveExtraJumps(1);

        Creature creature = player.getWorld().spawn(player.getLocation(), Zombie.class);
        creature.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, config.getInt("Clone.Speed")));
        creature.setMaxHealth(config.getInt("Clone.Health"));
        creature.getEquipment().setItemInHand(null);
        creature.setCustomName(MessageUtils.color("&8Shadow Clone"));
        creature.setCustomNameVisible(true);

        DisguiseAPI.disguiseEntity(creature, new PlayerDisguise(player.getName()));

        ShadowClone clone = new ShadowClone(plugin, this, config, creature, clones);
        plugin.getTeamManager().getPlayerTeam(player).addEntity(clone.creature);
        clones.add(clone);

        if (clones.size() > config.getInt("Clone.Limit")) {
            clones.get(0).destroy();
        }

        clone.creature.setVelocity(direction.multiply(config.getDouble("Clone.Velocity")));

        clone.runTaskTimer(plugin, 0, 10);
        Bukkit.getPluginManager().registerEvents(clone, plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (clone.creature.isValid()) {
                clone.destroy();
            }
        }, 400);
    }

    @Override
    public void destroy() {
        super.destroy();
        new ArrayList<>(clones).forEach(ShadowClone::destroy);
    }

    private static class ShadowClone extends BukkitRunnable implements Listener {
        private final SuperSmashLegends plugin;
        private final ShadowCloneJutsu ability;
        private final Section config;
        private final Creature creature;
        private final List<ShadowClone> clones;
        private Location lastShurikenLocation;
        private LivingEntity target;
        private BukkitTask rasenganTask;

        public ShadowClone(SuperSmashLegends plugin, ShadowCloneJutsu ability, Section config, Creature creature, List<ShadowClone> friends) {
            this.plugin = plugin;
            this.config = config;
            this.creature = creature;
            this.ability = ability;
            this.clones = friends;
        }

        private void destroy() {
            this.clones.remove(this);
            this.creature.remove();
            HandlerList.unregisterAll(this);
            Optional.ofNullable(rasenganTask).ifPresent(BukkitTask::cancel);
            this.creature.getWorld().playSound(this.creature.getLocation(), Sound.WITHER_HURT, 2, 1);
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(this.creature.getLocation(), 1.5, 10, 0.1);
            this.ability.getPlayer().playSound(this.ability.getPlayer().getLocation(), Sound.WITHER_IDLE, 2, 1);
            this.plugin.getTeamManager().getPlayerTeam(this.ability.getPlayer()).removeEntity(this.creature);
        }

        private void endRasengan() {
            rasenganTask.cancel();
            Rasengan.end(this.creature);
            this.creature.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, config.getInt("Clone.Speed")));
        }

        @EventHandler
        public void onRasenganStart(Rasengan.RasenganStartEvent event) {
            if (event.getRasengan().getPlayer() != this.ability.getPlayer()) return;

            event.apply(this.creature, config.getInt("Clone.Speed") + config.getInt("Clone.Rasengan.Speed"));
            ShadowClone instance = this;

            rasenganTask = new BukkitRunnable() {
                int ticksCharged = 0;

                @Override
                public void run() {

                    if (++ticksCharged >= config.getInt("Clone.Rasengan.Lifespan")) {
                        endRasengan();
                        return;
                    }

                    Rasengan.display(instance.creature);
                }

            }.runTaskTimer(plugin, 0, 0);
        }

        @EventHandler
        public void onRasenshurikenDisplay(Rasenshuriken.RasenshurikenDisplayEvent event) {
            if (event.getRasenshuriken().getPlayer() != this.ability.getPlayer()) return;

            double height = config.getDouble("Clone.Rasenshuriken.Height");
            this.lastShurikenLocation = EntityUtils.top(this.creature).add(0, height, 0);
            Rasenshuriken.display(this.lastShurikenLocation, false, config);
            this.creature.getWorld().playSound(this.lastShurikenLocation, Sound.FUSE, 0.5f, 1);
        }

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            if (event.getEntity() == this.creature) {
                this.destroy();
            }
        }

        @EventHandler
        public void onDamage(AttributeDamageEvent event) {
            if (event.getAttribute() == this.ability && this.clones.stream().anyMatch(clone -> clone.creature == event.getVictim())) {
                event.setCancelled(true);
            }
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
            shuriken.getDamage().setDamage(shuriken.getDamage().getDamage() * config.getDouble("Clone.Rasenshuriken.DamageMultiplier"));
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
            EntityFinder finder = new EntityFinder(plugin, new DistanceSelector(config.getDouble("Clone.Vision")));

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
            Location curr = this.creature.getEyeLocation().subtract(0, 0.5, 0);
            Vector step = this.creature.getEyeLocation().getDirection().multiply(0.1);

            Team team = plugin.getTeamManager().getPlayerTeam(this.ability.getPlayer());

            while (!found && stepped < 3) {
                curr.add(step);
                stepped += 0.1;

                for (LivingEntity target : this.creature.getWorld().getLivingEntities()) {
                    if (TeamPreference.FRIENDLY.validate(team, target)) return;

                    if (BlockUtils.isLocationInsideBox(curr, NmsUtils.getLiving(target).getBoundingBox())) {
                        Damage damage;

                        if (rasenganTask == null) {
                            damage = Damage.Builder.fromConfig(config.getSection("Clone.Melee"), step)
                                    .setDamage(this.ability.getKit().getDamage()).build();

                        } else {
                            damage = Damage.Builder.fromConfig(config.getSection("Rasengan"), step).build();

                            Rasengan.displayAttackEffect(this.creature);
                            endRasengan();
                        }

                        if (plugin.getDamageManager().attemptAttributeDamage(target, damage, this.ability)) {
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
}