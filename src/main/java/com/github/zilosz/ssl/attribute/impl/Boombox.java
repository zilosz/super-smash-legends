package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.event.projectile.ProjectileHitBlockEvent;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.CustomProjectile;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.util.block.BlockHitResult;
import com.github.zilosz.ssl.util.block.BlockRay;
import com.github.zilosz.ssl.util.effects.ParticleMaker;
import com.github.zilosz.ssl.util.entity.EntityUtils;
import com.github.zilosz.ssl.util.entity.finder.EntityFinder;
import com.github.zilosz.ssl.util.entity.finder.selector.impl.DistanceSelector;
import com.github.zilosz.ssl.util.file.YamlReader;
import com.github.zilosz.ssl.util.math.MathUtils;
import com.github.zilosz.ssl.util.math.VectorUtils;
import com.github.zilosz.ssl.util.message.Chat;
import com.github.zilosz.ssl.util.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.NoteColor;

import java.text.DecimalFormat;

public class Boombox extends RightClickAbility {
  private boolean isPlaced;
  private boolean canPlace = true;
  private int charge;
  private Block block;
  private BukkitTask canPlaceAgainTask;

  private BukkitTask healthTask;
  private double health;

  private int clickTicksLeft;
  private BukkitTask allowClickTask;

  private Hologram hologram;
  private TextHologramLine healthLine;

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    if (super.invalidate(event) || isPlaced) return true;

    if (!canPlace) {
      Chat.ABILITY.send(player, "&7Touch the ground before re-using Boombox.");
    }

    return !canPlace;
  }

  @Override
  public void onClick(PlayerInteractEvent event) {
    isPlaced = true;

    Location source = player.getEyeLocation();
    BlockRay blockRay = new BlockRay(source);
    blockRay.cast(config.getInt("PlaceReach"));
    block = blockRay.getEmptyLoc().getBlock();
    block.setType(Material.JUKEBOX);

    player.getWorld().playSound(block.getLocation(), Sound.DIG_WOOD, 2, 1);
    player.getWorld().playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 3, 0.5f);

    Location above = block.getLocation().add(0.5, 1.7, 0.5);
    hologram = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(above);
    String color = kit.getColor().getChatSymbol();
    String title = String.format("&f%s's %s&lBoombox", player.getName(), color);
    hologram.getLines().appendText(MessageUtils.color(title));

    health = getMaxHealth();
    updateHealth(0);

    double healthLossPerTick = health / config.getInt("MaxDuration");

    healthTask = Bukkit
        .getScheduler()
        .runTaskTimer(SSL.getInstance(), () -> updateHealth(healthLossPerTick), 1, 1);
  }

  private double getMaxHealth() {
    return config.getDouble("Health");
  }

  private void updateHealth(double loss) {
    health -= loss;

    if (health <= 0) {
      reset(true);
      return;
    }

    player.setExp((float) (health / getMaxHealth()));
    String bar = MessageUtils.progressBar("|", "|", "&a", "&c", health, getMaxHealth(), 20);

    if (hologram.getLines().size() == 1) {
      healthLine = hologram.getLines().insertText(0, bar);
    }
    else {
      healthLine.setText(bar);
    }
  }

  private void reset(boolean startCanPlaceTask) {
    if (!isPlaced) return;

    startCooldown();

    if (startCanPlaceTask) {
      canPlace = false;

      canPlaceAgainTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {

        if (EntityUtils.isPlayerGrounded(player)) {
          canPlace = true;
          canPlaceAgainTask.cancel();
        }
      }, 0, 0);
    }
    else {
      canPlace = true;

      if (canPlaceAgainTask != null) {
        canPlaceAgainTask.cancel();
      }
    }

    isPlaced = false;

    block.setType(Material.AIR);
    charge = 0;

    player.setExp(0);

    hologram.delete();
    healthTask.cancel();
    clickTicksLeft = 0;

    if (allowClickTask != null) {
      allowClickTask.cancel();
    }

    player.getWorld().playSound(block.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 0.8f);
  }

  @Override
  public void deactivate() {
    reset(false);
    super.deactivate();
  }

  @EventHandler
  public void onProjectileHitBlock(ProjectileHitBlockEvent event) {
    if (!isPlaced) return;
    if (!(event.getResult().getBlock().equals(block))) return;

    CustomProjectile<?> projectile = event.getProjectile();

    if (projectile.getAttackInfo().getType() == AttackType.MIX_TAPE_DROP) {
      explode();
    }
    else {
      updateHealth(projectile.getAttack().getDamage().getDamage());
      player.getWorld().playSound(block.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1.5f);
    }
  }

  private void explode() {

    for (int i = 0; i < 3; i++) {
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.EXPLOSION_HUGE);
      new ParticleMaker(particle).setSpread(0.5).show(block.getLocation());
    }

    player.getWorld().playSound(block.getLocation(), Sound.EXPLODE, 3, 2);
    player.getWorld().playSound(block.getLocation(), Sound.NOTE_SNARE_DRUM, 3, 0.5f);

    Section mixTapeConfig = config.getSection("Mixtape");
    double radius = mixTapeConfig.getDouble("Radius");

    EntityFinder finder = new EntityFinder(new DistanceSelector(radius))
        .setTeamPreference(TeamPreference.ANY)
        .setAvoidsUser(false);

    Location center = block.getLocation().add(0.5, 0.5, 0.5);

    finder.findAll(player, center).forEach(target -> {
      Vector direction = VectorUtils.fromTo(center, target.getLocation());

      double distance = target.getLocation().distance(center);
      double damage = YamlReader.decreasingValue(mixTapeConfig, "Damage", distance, radius);
      double kb = YamlReader.decreasingValue(mixTapeConfig, "Kb", distance, radius);

      Attack attack = YamlReader.attack(mixTapeConfig, direction, getDisplayName());
      attack.getDamage().setDamage(damage);
      attack.getKb().setKb(kb);

      AttackInfo attackInfo = new AttackInfo(AttackType.BOOMBOX_EXPLODE, this);
      SSL.getInstance().getDamageManager().attack(target, attack, attackInfo);
    });

    reset(true);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isPlaced) return;
    if (event.getClickedBlock() == null) return;
    if (!event.getClickedBlock().equals(block)) return;
    if (SSL.getInstance().getGameManager().isSpectator(event.getPlayer())) return;

    if (event.getPlayer() != player) {
      Kit enemyKit = SSL.getInstance().getKitManager().getSelectedKit(event.getPlayer());
      updateHealth(enemyKit.getDamage());
      return;
    }

    if (clickTicksLeft > 0) {
      String secLeft = new DecimalFormat("#.#").format(clickTicksLeft / 20.0);
      Chat.ABILITY.send(player, String.format("&7You must wait &e%s &7seconds to click.", secLeft));
      return;
    }

    if (allowClickTask != null) {
      allowClickTask.cancel();
    }

    clickTicksLeft = config.getInt("ClickDelay");
    allowClickTask =
        Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> clickTicksLeft--, 1, 1);

    int maxPunches = config.getInt("MaxPunches");

    float pitch = (float) MathUtils.incVal(0.5, 2, maxPunches - 1, charge);
    player.getWorld().playSound(block.getLocation(), Sound.NOTE_PLING, 2, pitch);

    for (int i = 0; i < 5; i++) {
      NoteColor note = new NoteColor((int) MathUtils.randRange(0, 25));
      ParticleBuilder particle = new ParticleBuilder(ParticleEffect.NOTE).setParticleData(note);
      new ParticleMaker(particle)
          .setSpread(0.4, 0.4, 0.4)
          .show(block.getLocation().add(0.5, 1.6, 0.5));
    }

    Vector direction = player.getEyeLocation().getDirection();
    Location source = block.getLocation().add(0.5, 0.5, 0.5).add(direction).setDirection(direction);

    launch(true, source);

    int count = config.getInt("Count");
    double conicAngle = config.getDouble("ConicAngle");

    for (Vector vector : VectorUtils.conicVectors(source, conicAngle, count - 1)) {
      Location loc = source.clone().setDirection(vector);
      launch(false, loc);
    }

    if (++charge == maxPunches) {
      reset(true);
    }
  }

  private void launch(boolean first, Location source) {
    Section settings = config.getSection("Projectile");
    AttackInfo attackInfo = new AttackInfo(AttackType.BOOMBOX_PROJECTILE, this);
    MusicDiscProjectile projectile = new MusicDiscProjectile(settings, attackInfo);
    projectile.setOverrideLocation(source);

    if (first) {
      projectile.setSpread(0);
    }

    projectile.launch();
  }

  @EventHandler
  public void onAbilityUse(AbilityUseEvent event) {
    if (!(event.getAbility() instanceof DjsPassion)) return;
    if (event.getAbility().getPlayer() != player) return;
    if (!isPlaced) return;

    Location tpLocation = block.getLocation().add(0, 1, 0);

    if (player.isSneaking()) {
      tpLocation = block.getLocation().subtract(0, 2, 0);
    }

    tpLocation.add(0.5, 0, 0.5);

    boolean solid1 = tpLocation.getBlock().getType().isSolid();
    boolean solid2 = tpLocation.clone().add(0, 1, 0).getBlock().getType().isSolid();

    if (!solid1 && !solid2) {
      player.teleport(tpLocation.setDirection(player.getEyeLocation().getDirection()));

      player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 2, 1);
      player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 2);

      ((DjsPassion) event.getAbility()).setSucceeded(true);
    }
  }

  private static class MusicDiscProjectile extends ItemProjectile {
    private final int note = (int) MathUtils.randRange(0, 25);

    public MusicDiscProjectile(Section config, AttackInfo attackInfo) {
      super(config, attackInfo);
    }

    @Override
    public void onLaunch() {
      entity.getWorld().playSound(entity.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 2);
    }

    @Override
    public void onBlockHit(BlockHitResult result) {
      showEffect();
    }

    @Override
    public void onTargetHit(LivingEntity target) {
      showEffect();
    }

    @Override
    public void onTick() {
      ParticleBuilder particle =
          new ParticleBuilder(ParticleEffect.NOTE).setParticleData(new NoteColor(note));
      new ParticleMaker(particle).show(entity.getLocation());
    }

    private void showEffect() {
      new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(entity.getLocation());
      entity.getWorld().playSound(entity.getLocation(), Sound.NOTE_PLING, 2, 2);
    }
  }
}
