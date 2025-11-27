package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attack.Damage;
import com.github.zilosz.ssl.attack.KnockBack;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.event.attack.DamageEvent;
import com.github.zilosz.ssl.util.block.BlockUtils;
import com.github.zilosz.ssl.util.collection.CollectionUtils;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class Scarecrow extends RightClickAbility {
  private final Set<LivingEntity> affectedTargets = new HashSet<>();
  private NPC scarecrow;
  private int ticksUntilCanTp;
  private int ticksLeft;
  private BukkitTask removeTask;
  private BukkitTask mainTask;
  private Hologram npcHologram;
  private TextHologramLine tpHintLine;

  @Override
  public void onClick(PlayerInteractEvent event) {

    if (scarecrow != null && scarecrow.isSpawned()) {

      if (ticksUntilCanTp == 0) {
        tpHintLine.setText(getTpHint(false));

        Location oldPlayerLoc = player.getLocation();
        player.teleport(scarecrow.getStoredLocation());
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0.75f);

        scarecrow.getEntity().teleport(oldPlayerLoc);
        oldPlayerLoc.getWorld().playSound(oldPlayerLoc, Sound.ENDERMAN_TELEPORT, 1, 2);

        ticksUntilCanTp = config.getInt("ActiveTpDelay");
      }
      else {
        String secUntilTp = new DecimalFormat("#.#").format(ticksUntilCanTp / 20.0);
        Chat.ABILITY.send(player,
            String.format("&7You can teleport in &e%s &7seconds.", secUntilTp)
        );
      }
    }
    else {
      sendUseMessage();

      player.getWorld().playSound(player.getLocation(), Sound.WITHER_SPAWN, 0.5f, 2);
      player.getWorld().playSound(player.getLocation(), Sound.DIG_GRAVEL, 1, 0.5f);

      NPCRegistry registry = SSL.getInstance().getNpcRegistry();
      scarecrow = registry.createNPC(EntityType.PLAYER, player.getDisplayName());
      scarecrow.setProtected(false);

      kit.getSkin().applyToNpc(scarecrow);
      scarecrow.removeTrait(LookClose.class);

      Location eyeLoc = player.getEyeLocation();
      Vector direction = eyeLoc.getDirection();

      Location spawnLoc =
          eyeLoc.clone().add(direction.clone().multiply(config.getDouble("EyeDistance")));
      spawnLoc.setPitch(0);
      scarecrow.spawn(spawnLoc);

      LivingEntity scarecrowPlayer = (LivingEntity) scarecrow.getEntity();
      SSL.getInstance().getTeamManager().addEntityToTeam(scarecrowPlayer, player);

      double health = config.getDouble("Health");
      scarecrowPlayer.setMaxHealth(health);
      scarecrowPlayer.setHealth(health);

      npcHologram =
          HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(getHologramLocation());
      npcHologram.getLines().appendText(MessageUtils.color(config.getString("Hologram.Title")));
      tpHintLine = npcHologram.getLines().appendText(getTpHint(false));

      VisibilitySettings settings = npcHologram.getVisibilitySettings();
      settings.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
      settings.setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);

      scarecrow.getEntity().setVelocity(direction.multiply(config.getDouble("LaunchVelocity")));

      int lifetime = config.getInt("CrowLifetime");
      ticksLeft = lifetime;

      removeTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
        reset();
        startCooldown();
      }, lifetime);

      ticksUntilCanTp = config.getInt("InitialTpDelay");
      EntitySelector selector = new DistanceSelector(getEffectRange());

      double accuracy = config.getDouble("VoidDetectionAccuracy");

      mainTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

        if (Math.abs(scarecrow.getStoredLocation().getY()) <= accuracy) {
          reset();
          startCooldown();
          return;
        }

        npcHologram.setPosition(getHologramLocation());

        if (ticksUntilCanTp > 0 && --ticksUntilCanTp == 0) {
          player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.5f);
          tpHintLine.setText(getTpHint(true));
        }

        player.setExp((float) --ticksLeft / lifetime);

        Location location = scarecrow.getStoredLocation();

        Predicate<LivingEntity> predicate = entity -> !selector.containsEntity(location, entity);
        CollectionUtils.clearWhileIterating(affectedTargets, this::removeEffect, predicate);

        EntityFinder finder = new EntityFinder(selector)
            .avoid((LivingEntity) scarecrow.getEntity())
            .avoidAll(affectedTargets);

        finder.findAll(player, location).forEach(target -> {
          target.getWorld().playSound(target.getLocation(), Sound.DIG_GRASS, 1.2f, 0.8f);
          player.playSound(player.getLocation(), Sound.ENDERMAN_HIT, 1, 2);

          scarecrow.faceLocation(target.getLocation());

          Section slowConfig = config.getSection("Slowness");
          PotionEffect effect = YamlReader.potionEffect(slowConfig, PotionEffectType.SLOW);
          new PotionEffectEvent(target, effect).apply();

          target.setMetadata("pumpkin", new FixedMetadataValue(SSL.getInstance(), ""));
          affectedTargets.add(target);

          if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            targetPlayer.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));

          }
          else {
            target.setPassenger(BlockUtils.spawnFallingBlock(target.getLocation(),
                Material.PUMPKIN
            ));
          }
        });
      }, 1, 0);
    }
  }

  private String getTpHint(boolean canTp) {
    return MessageUtils.color(config.getString("Hologram.TpHint." + (canTp ? "Yes" : "No")));
  }

  private Location getHologramLocation() {
    return EntityUtils
        .top(scarecrow.getEntity())
        .add(0, config.getDouble("Hologram.HeightAboveHead"), 0);
  }

  private void reset() {
    if (mainTask == null) return;

    player.setExp(0);

    removeTask.cancel();
    mainTask.cancel();

    Optional.ofNullable(scarecrow.getEntity()).ifPresent(entity -> {
      player.playSound(player.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 2);
      kit.getDeathNoise().playForAll(entity.getLocation());

      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE);
      new ParticleMaker(particle).solidSphere(entity.getLocation(), 0.75, 15, 0.25);
      entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_HURT, 1, 0.5f);

      SSL.getInstance().getTeamManager().removeEntityFromTeam((Player) entity);
    });

    npcHologram.delete();
    scarecrow.destroy();

    CollectionUtils.clearWhileIterating(affectedTargets, this::removeEffect);
  }

  private void removeEffect(LivingEntity entity) {
    entity.removePotionEffect(PotionEffectType.SLOW);
    entity.removeMetadata("pumpkin", SSL.getInstance());

    if (entity instanceof Player) {
      ((HumanEntity) entity).getInventory().setHelmet(new ItemStack(Material.AIR));
    }
    else {
      Optional.ofNullable(entity.getPassenger()).ifPresent(Entity::remove);
    }
  }

  private double getEffectRange() {
    return config.getDouble("EffectRange");
  }

  @Override
  public void deactivate() {
    super.deactivate();
    reset();
  }

  @EventHandler
  public void onMelee(AttackEvent event) {
    if (event.getInfo().getAttribute().getPlayer() != player) return;
    if (event.getInfo().getType() != AttackType.MELEE) return;
    if (!affectedTargets.contains(event.getVictim())) return;

    Section boostConfig = config.getSection("MeleePumpkinBoost");

    Damage damage = event.getAttack().getDamage();
    damage.setDamage(damage.getDamage() * boostConfig.getDouble("Damage"));

    KnockBack kb = event.getAttack().getKb();
    kb.setKb(kb.getKb() * boostConfig.getDouble("Kb"));

    event
        .getVictim()
        .getWorld()
        .playSound(event.getVictim().getLocation(), Sound.WITHER_HURT, 1, 1);
  }

  @EventHandler
  public void onDamage(DamageEvent event) {
    if (scarecrow != null && event.getVictim() == scarecrow.getEntity()) {
      kit.getHurtNoise().playForAll(scarecrow.getStoredLocation());
    }
  }

  @EventHandler
  public void onNpcDeath(NPCDeathEvent event) {
    if (scarecrow != null && event.getNPC() == scarecrow) {
      reset();
      startCooldown();
      ((PlayerDeathEvent) event.getEvent()).setDeathMessage("");
    }
  }
}
