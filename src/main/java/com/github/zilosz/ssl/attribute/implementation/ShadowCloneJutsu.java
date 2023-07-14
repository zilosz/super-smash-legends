package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.AbilityType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.utils.NmsUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ShadowCloneJutsu extends RightClickAbility {
    private final Map<NPC, CloneData> clones = new HashMap<>();
    private NPC lastSpawnedClone;
    private boolean isPlayerUsingRasengan = false;

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_SPAWN, 0.5f, 1.5f);

        Location eyeLoc = this.player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        this.player.setVelocity(direction.clone().multiply(-this.config.getDouble("Recoil")));

        if (this.clones.size() == this.config.getInt("Limit") && this.lastSpawnedClone != null) {
            this.destroyClone(this.lastSpawnedClone);
        }

        NPC npc = SSL.getInstance().getNpcRegistry().createNPC(EntityType.PLAYER, this.player.getDisplayName());
        npc.setProtected(false);

        this.kit.getSkin().applyToNpc(npc);
        npc.removeTrait(LookClose.class);

        NavigatorParameters params = npc.getNavigator().getLocalParameters();
        params.baseSpeed(this.config.getFloat("WalkSpeed"));

        eyeLoc.setPitch(0);
        npc.spawn(eyeLoc);
        this.lastSpawnedClone = npc;

        SSL.getInstance().getTeamManager().getPlayerTeam(this.player).addEntity(this.getNpcPlayer(npc));

        Player npcPlayer = this.getNpcPlayer(npc);
        double health = this.config.getDouble("Health");
        npcPlayer.setMaxHealth(health);
        npcPlayer.setHealth(health);
        npcPlayer.setVelocity(direction.multiply(this.config.getDouble("Velocity")));

        BukkitTask removeTask = Bukkit.getScheduler()
                .runTaskLater(SSL.getInstance(), () -> this.destroyClone(npc), this.config.getInt("Duration"));

        double accuracy = this.config.getDouble("VoidDetectionAccuracy");

        BukkitTask tickTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

            if (Math.abs(npc.getEntity().getLocation().getY()) <= accuracy) {
                this.destroyClone(npc);
                return;
            }

            EntityFinder finder = new EntityFinder(new DistanceSelector(this.config.getDouble("Vision")));

            finder.findClosest(this.player, npc.getStoredLocation()).ifPresentOrElse(target -> {
                npc.getNavigator().setTarget(target, false);
                npc.faceLocation(target.getLocation());
            }, () -> {
                npc.faceLocation(npc.getStoredLocation().add(this.player.getEyeLocation().getDirection()));
            });
        }, 0, 0);

        this.clones.put(npc, new CloneData(removeTask, tickTask));
    }

    private void destroyClone(NPC npc) {
        ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
        new ParticleMaker(particle).solidSphere(npc.getStoredLocation(), 1.5, 10, 0.1);

        this.player.getWorld().playSound(npc.getStoredLocation(), Sound.ZOMBIE_PIG_HURT, 1, 1.5f);
        this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_PIG_HURT, 1, 1.5f);

        SSL.getInstance().getTeamManager().getPlayerTeam(this.player).removeEntity(this.getNpcPlayer(npc));
        npc.destroy();

        CloneData clone = this.clones.get(npc);
        clone.removeTask.cancel();
        clone.tickTask.cancel();
        this.clones.remove(npc);
    }

    private Player getNpcPlayer(NPC npc) {
        return (Player) npc.getEntity();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        new LinkedHashMap<>(this.clones).keySet().forEach(this::destroyClone);
    }

    @EventHandler
    public void onNpcDeath(NPCDeathEvent event) {
        if (this.clones.containsKey(event.getNPC())) {
            this.destroyClone(event.getNPC());
            ((PlayerDeathEvent) event.getEvent()).setDeathMessage("");
        }
    }

    @EventHandler
    public void onRasenganStart(Rasengan.RasenganStartEvent event) {
        this.distributeNpcEntityAction(event.getRasengan(), Rasengan::start);
        this.isPlayerUsingRasengan = true;
    }

    private <T extends Ability> void distributeNpcEntityAction(T ability, BiConsumer<T, Player> action) {
        if (ability.getPlayer() == this.player) {
            this.clones.keySet().forEach(npc -> action.accept(ability, this.getNpcPlayer(npc)));
        }
    }

    @EventHandler
    public void onRasenganDisplay(Rasengan.RasenganDisplayEvent event) {
        this.distributeNpcEntityAction(event.getRasengan(), Rasengan::display);
    }

    @EventHandler
    public void onRasenganLeap(Rasengan.RasenganLeapEvent event) {
        this.distributeNpcEntityAction(event.getRasengan(), Rasengan::leap);
    }

    @EventHandler
    public void onRasenganEnd(Rasengan.RasenganEndEvent event) {
        this.distributeNpcEntityAction(event.getRasengan(), Rasengan::end);
        this.isPlayerUsingRasengan = false;
    }

    @EventHandler
    public void onRasenshurikenDisplay(Rasenshuriken.RasenshurikenDisplayEvent event) {
        this.distributeNpcEntityAction(event.getRasenshuriken(), Rasenshuriken::displayOnHead);
    }

    @EventHandler
    public void onRasenshurikenLaunch(Rasenshuriken.RasenshurikenLaunchEvent event) {
        this.distributeNpcEntityAction(event.getRasenshuriken(), (rasenshuriken, entity) -> {
            rasenshuriken.launch(entity, this, AttackType.SHADOW_CLONE_RASENSHURIKEN);
        });
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (event.getAttackInfo().getAttribute() == this) {
            Attack attack = event.getAttack();
            double multiplier = this.config.getDouble("AttackMultiplier");
            attack.getDamage().setDamage(attack.getDamage().getDamage() * multiplier);
            attack.getKb().setKb(attack.getKb().getKb() * multiplier);
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getPlayer() != this.player) return;

        for (NPC npc : this.clones.keySet()) {
            Player cloneEntity = this.getNpcPlayer(npc);
            NmsUtils.broadcastPacket(new PacketPlayOutAnimation(NmsUtils.getPlayer(cloneEntity), 0));

            Optional.ofNullable(npc.getNavigator().getEntityTarget()).ifPresent(target -> {
                LivingEntity livingTarget = (LivingEntity) target.getTarget();
                double distanceSquared = npc.getStoredLocation().distanceSquared(livingTarget.getLocation());

                double meleeRange = this.config.getDouble("MeleeRange");

                if (distanceSquared <= meleeRange * meleeRange && cloneEntity.hasLineOfSight(livingTarget)) {
                    Attack attack = this.kit.getMelee().createAttack(cloneEntity);

                    if (this.isPlayerUsingRasengan) {
                        Section config = SSL.getInstance().getResources().getAbilityConfig(AbilityType.RASENGAN);
                        Rasengan.modifyMeleeAttack(attack, config);
                    }

                    attack.setName(this.getDisplayName());
                    AttackInfo attackInfo = new AttackInfo(AttackType.MELEE, this);

                    if (SSL.getInstance().getDamageManager().attack(livingTarget, attack, attackInfo)) {
                        this.player.playSound(this.player.getLocation(), Sound.WITHER_HURT, 0.5f, 1);
                        livingTarget.getWorld().playSound(livingTarget.getLocation(), Sound.WITHER_HURT, 0.5f, 1);

                        if (this.isPlayerUsingRasengan) {
                            Rasengan.displayAttack(livingTarget);
                        }
                    }
                }
            });
        }
    }

    private static class CloneData {
        private final BukkitTask removeTask;
        private final BukkitTask tickTask;

        public CloneData(BukkitTask removeTask, BukkitTask tickTask) {
            this.removeTask = removeTask;
            this.tickTask = tickTask;
        }
    }
}
