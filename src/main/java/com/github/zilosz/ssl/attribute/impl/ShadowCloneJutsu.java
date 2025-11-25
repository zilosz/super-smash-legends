package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.AbilityType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.util.NmsUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.RequiredArgsConstructor;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ShadowCloneJutsu extends RightClickAbility {
  private final Map<NPC, CloneData> clones = new HashMap<>();
  private NPC lastSpawnedClone;
  private boolean isPlayerUsingRasengan;

  @Override
  public void onClick(PlayerInteractEvent event) {
    player.getWorld().playSound(player.getLocation(), Sound.WITHER_SPAWN, 0.5f, 1.5f);

    Location eyeLoc = player.getEyeLocation();
    Vector direction = eyeLoc.getDirection();
    player.setVelocity(direction.clone().multiply(-config.getDouble("Recoil")));

    if (clones.size() == config.getInt("Limit") && lastSpawnedClone != null) {
      destroyClone(lastSpawnedClone);
    }

    NPC npc =
        SSL.getInstance().getNpcRegistry().createNPC(EntityType.PLAYER, player.getDisplayName());
    npc.setProtected(false);

    kit.getSkin().applyToNpc(npc);
    npc.removeTrait(LookClose.class);

    NavigatorParameters params = npc.getNavigator().getLocalParameters();
    params.baseSpeed(config.getFloat("WalkSpeed"));

    eyeLoc.setPitch(0);
    npc.spawn(eyeLoc);
    lastSpawnedClone = npc;

    SSL.getInstance().getTeamManager().addEntityToTeam(getNpcPlayer(npc), player);

    Player npcPlayer = getNpcPlayer(npc);
    double health = config.getDouble("Health");
    npcPlayer.setMaxHealth(health);
    npcPlayer.setHealth(health);
    npcPlayer.setVelocity(direction.multiply(config.getDouble("Velocity")));

    BukkitTask removeTask = Bukkit
        .getScheduler()
        .runTaskLater(SSL.getInstance(), () -> destroyClone(npc), config.getInt("Duration"));

    double accuracy = config.getDouble("VoidDetectionAccuracy");

    BukkitTask tickTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

      if (Math.abs(npc.getEntity().getLocation().getY()) <= accuracy) {
        destroyClone(npc);
        return;
      }

      EntityFinder finder = new EntityFinder(new DistanceSelector(config.getDouble("Vision")));

      finder.findClosest(player, npc.getStoredLocation()).ifPresentOrElse(target -> {
        npc.getNavigator().setTarget(target, false);
        npc.faceLocation(target.getLocation());
      }, () -> {
        npc.faceLocation(npc.getStoredLocation().add(player.getEyeLocation().getDirection()));
      });
    }, 0, 0);

    clones.put(npc, new CloneData(removeTask, tickTask));
  }

  private void destroyClone(NPC npc) {
    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE).setSpeed(0);
    new ParticleMaker(particle).solidSphere(npc.getStoredLocation(), 1.5, 10, 0.1);

    player.getWorld().playSound(npc.getStoredLocation(), Sound.ZOMBIE_PIG_HURT, 1, 1.5f);
    player.playSound(player.getLocation(), Sound.ZOMBIE_PIG_HURT, 1, 1.5f);

    SSL.getInstance().getTeamManager().removeEntityFromTeam(getNpcPlayer(npc));
    npc.destroy();

    CloneData clone = clones.get(npc);
    clone.removeTask.cancel();
    clone.tickTask.cancel();
    clones.remove(npc);
  }

  private Player getNpcPlayer(NPC npc) {
    return (Player) npc.getEntity();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    new HashSet<>(clones.keySet()).forEach(this::destroyClone);
  }

  @EventHandler
  public void onNpcDeath(NPCDeathEvent event) {
    if (clones.containsKey(event.getNPC())) {
      destroyClone(event.getNPC());
      ((PlayerDeathEvent) event.getEvent()).setDeathMessage("");
    }
  }

  @EventHandler
  public void onRasenganStart(Rasengan.RasenganStartEvent event) {
    distributeNPCAction(event.getRasengan(), Rasengan::start);
    isPlayerUsingRasengan = true;
  }

  private <T extends Ability> void distributeNPCAction(
      T ability, BiConsumer<T, Player> action
  ) {
    if (ability.getPlayer() == player) {
      clones.keySet().forEach(npc -> action.accept(ability, getNpcPlayer(npc)));
    }
  }

  @EventHandler
  public void onRasenganDisplay(Rasengan.RasenganDisplayEvent event) {
    distributeNPCAction(event.getRasengan(), Rasengan::display);
  }

  @EventHandler
  public void onRasenganLeap(Rasengan.RasenganLeapEvent event) {
    distributeNPCAction(event.getRasengan(), Rasengan::leap);
  }

  @EventHandler
  public void onRasenganEnd(Rasengan.RasenganEndEvent event) {
    distributeNPCAction(event.getRasengan(), Rasengan::end);
    isPlayerUsingRasengan = false;
  }

  @EventHandler
  public void onRasenshurikenDisplay(Rasenshuriken.RasenshurikenDisplayEvent event) {
    distributeNPCAction(event.getRasenshuriken(), Rasenshuriken::displayOnHead);
  }

  @EventHandler
  public void onRasenshurikenLaunch(Rasenshuriken.RasenshurikenLaunchEvent event) {
    distributeNPCAction(event.getRasenshuriken(), (rasenshuriken, entity) -> {
      rasenshuriken.launch(entity, this, AttackType.SHADOW_CLONE_RASENSHURIKEN);
    });
  }

  @EventHandler
  public void onAttack(AttackEvent event) {
    if (event.getInfo().getAttribute() == this) {
      Attack attack = event.getAttack();
      double multiplier = config.getDouble("AttackMultiplier");
      attack.getDamage().setDamage(attack.getDamage().getDamage() * multiplier);
      attack.getKb().setKb(attack.getKb().getKb() * multiplier);
    }
  }

  @EventHandler
  public void onPlayerAnimation(PlayerAnimationEvent event) {
    if (event.getPlayer() != player) return;

    for (NPC npc : clones.keySet()) {
      Player cloneEntity = getNpcPlayer(npc);
      NmsUtils.broadcastPacket(new PacketPlayOutAnimation(NmsUtils.getPlayer(cloneEntity), 0));

      Optional.ofNullable(npc.getNavigator().getEntityTarget()).ifPresent(target -> {
        LivingEntity livingTarget = (LivingEntity) target.getTarget();
        double distanceSquared =
            npc.getStoredLocation().distanceSquared(livingTarget.getLocation());

        double meleeRange = config.getDouble("MeleeRange");

        if (distanceSquared <= meleeRange * meleeRange &&
            cloneEntity.hasLineOfSight(livingTarget)) {
          Attack attack = kit.getMelee().createAttack(cloneEntity);

          if (isPlayerUsingRasengan) {
            Section config =
                SSL.getInstance().getResources().getAbilityConfig(AbilityType.RASENGAN);
            Rasengan.modifyMeleeAttack(attack, config);
          }

          attack.setName(getDisplayName());
          AttackInfo attackInfo = new AttackInfo(AttackType.MELEE, this);

          if (SSL.getInstance().getDamageManager().attack(livingTarget, attack, attackInfo)) {
            player.playSound(player.getLocation(), Sound.WITHER_HURT, 0.5f, 1);
            livingTarget
                .getWorld()
                .playSound(livingTarget.getLocation(), Sound.WITHER_HURT, 0.5f, 1);

            if (isPlayerUsingRasengan) {
              Rasengan.displayAttack(livingTarget);
            }
          }
        }
      });
    }
  }

  @RequiredArgsConstructor
  private static class CloneData {
    private final BukkitTask removeTask;
    private final BukkitTask tickTask;
  }
}
