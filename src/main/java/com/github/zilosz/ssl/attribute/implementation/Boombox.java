package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.event.attribute.AbilityUseEvent;
import com.github.zilosz.ssl.event.projectile.ProjectileHitBlockEvent;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import com.github.zilosz.ssl.utils.message.Chat;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.block.BlockRay;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.minecraft.server.v1_8_R3.EnumParticle;
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

import java.text.DecimalFormat;

public class Boombox extends RightClickAbility {
    private boolean isPlaced = false;
    private boolean canPlace = true;
    private int charge = 0;
    private Block block;
    private BukkitTask canPlaceAgainTask;

    private BukkitTask healthTask;
    private double health;

    private int clickTicksLeft = 0;
    private BukkitTask allowClickTask;

    private Hologram hologram;
    private TextHologramLine healthLine;

    public Boombox(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        if (super.invalidate(event) || this.isPlaced) return true;

        if (!this.canPlace) {
            Chat.ABILITY.send(this.player, "&7Touch the ground before re-using Boombox.");
        }

        return !this.canPlace;
    }

    private void reset(boolean startCanPlaceTask) {
        if (!this.isPlaced) return;

        this.startCooldown();

        if (startCanPlaceTask) {
            this.canPlace = false;

            this.canPlaceAgainTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {

                if (EntityUtils.isPlayerGrounded(this.player)) {
                    this.canPlace = true;
                    this.canPlaceAgainTask.cancel();
                }
            }, 0, 0);

        } else {
            this.canPlace = true;

            if (this.canPlaceAgainTask != null) {
                this.canPlaceAgainTask.cancel();
            }
        }

        this.isPlaced = false;

        this.block.setType(Material.AIR);
        this.charge = 0;

        this.player.setExp(0);

        this.hologram.delete();
        this.healthTask.cancel();
        this.clickTicksLeft = 0;

        if (this.allowClickTask != null) {
            this.allowClickTask.cancel();
        }

        this.player.getWorld().playSound(this.block.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 0.8f);
    }

    @Override
    public void deactivate() {
        this.reset(false);
        super.deactivate();
    }

    private double getMaxHealth() {
        return this.config.getDouble("Health");
    }

    private void updateHealth(double loss) {
        this.health -= loss;

        if (this.health <= 0) {
            this.reset(true);
            return;
        }

        this.player.setExp((float) (this.health / this.getMaxHealth()));
        String bar = MessageUtils.progressBar("|", "|", "&a", "&c", this.health, this.getMaxHealth(), 20);

        if (this.hologram.getLines().size() == 1) {
            this.healthLine = this.hologram.getLines().insertText(0, bar);

        } else {
            this.healthLine.setText(bar);
        }
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.isPlaced = true;

        Location source = this.player.getEyeLocation();
        BlockRay blockRay = new BlockRay(source);
        blockRay.cast(this.config.getInt("PlaceReach"));
        this.block = blockRay.getEmptyDestination().getBlock();
        this.block.setType(Material.JUKEBOX);

        this.player.getWorld().playSound(this.block.getLocation(), Sound.DIG_WOOD, 2, 1);
        this.player.getWorld().playSound(this.player.getLocation(), Sound.NOTE_SNARE_DRUM, 3, 0.5f);

        Location above = this.block.getLocation().add(0.5, 1.7, 0.5);
        this.hologram = HolographicDisplaysAPI.get(this.plugin).createHologram(above);
        String color = this.kit.getColor().getChatSymbol();
        String title = String.format("&f%s's %s&lBoombox", this.player.getName(), color);
        this.hologram.getLines().appendText(MessageUtils.color(title));

        this.health = this.getMaxHealth();
        this.updateHealth(0);

        double healthLossPerTick = this.health / this.config.getInt("MaxDuration");
        this.healthTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.updateHealth(healthLossPerTick), 1, 1);
    }

    private void explode() {

        for (int i = 0; i < 3; i++) {
            new ParticleBuilder(EnumParticle.EXPLOSION_HUGE).setSpread(0.5f, 0.5f, 0.5f).show(this.block.getLocation());
        }

        this.player.getWorld().playSound(this.block.getLocation(), Sound.EXPLODE, 3, 2);
        this.player.getWorld().playSound(this.block.getLocation(), Sound.NOTE_SNARE_DRUM, 3, 0.5f);

        Section mixTapeConfig = this.config.getSection("Mixtape");
        double radius = mixTapeConfig.getDouble("Radius");
        EntitySelector selector = new DistanceSelector(radius);

        EntityFinder finder = new EntityFinder(this.plugin, selector)
                .setTeamPreference(TeamPreference.ANY).setAvoidsUser(false);

        Location center = this.block.getLocation().add(0.5, 0.5, 0.5);

        finder.findAll(this.player, center).forEach(target -> {
            Vector direction = VectorUtils.fromTo(center, target.getLocation());

            double distance = target.getLocation().distance(center);
            double damage = YamlReader.decLin(mixTapeConfig, "Damage", distance, radius);
            double kb = YamlReader.decLin(mixTapeConfig, "Kb", distance, radius);

            AttackSettings settings = new AttackSettings(mixTapeConfig, direction)
                    .modifyDamage(damageSettings -> damageSettings.setDamage(damage))
                    .modifyKb(kbSettings -> kbSettings.setKb(kb));

            this.plugin.getDamageManager().attack(target, this, settings);
        });

        this.reset(true);
    }

    @EventHandler
    public void onProjectileHitBlock(ProjectileHitBlockEvent event) {
        if (!this.isPlaced) return;
        if (!(event.getResult().getBlock().equals(this.block))) return;

        if (event.getProjectile() instanceof MixTapeDrop.MixTapeProjectile) {
            this.explode();
        }
    }

    private void launch(boolean first, Location source) {
        Section settings = this.config.getSection("Projectile");
        MusicDiscProjectile projectile = new MusicDiscProjectile(this.plugin, this, settings);
        projectile.setOverrideLocation(source);

        if (first) {
            projectile.setSpread(0);
        }

        projectile.launch();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isPlaced) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().equals(this.block)) return;
        if (event.getPlayer() != this.player) return;

        if (this.clickTicksLeft > 0) {
            String secLeft = new DecimalFormat("#.#").format(this.clickTicksLeft / 20.0);
            Chat.ABILITY.send(this.player, String.format("&7You must wait &e%s &7seconds to click.", secLeft));
            return;
        }

        if (this.allowClickTask != null) {
            this.allowClickTask.cancel();
        }

        this.clickTicksLeft = this.config.getInt("ClickDelay");
        this.allowClickTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.clickTicksLeft--, 1, 1);

        int maxPunches = this.config.getInt("MaxPunches");

        float pitch = (float) MathUtils.increasingLinear(0.5, 2, maxPunches - 1, this.charge);
        this.player.getWorld().playSound(this.block.getLocation(), Sound.NOTE_PLING, 2, pitch);

        for (int i = 0; i < 5; i++) {
            new ParticleBuilder(EnumParticle.NOTE)
                    .setSpread(0.3f, 0.3f, 0.3f).show(this.block.getLocation().add(0, 1.4, 0));
        }

        Vector direction = this.player.getEyeLocation().getDirection();
        Location source = this.block.getLocation().add(0.5, 0.5, 0.5).add(direction).setDirection(direction);

        this.launch(true, source);

        int count = this.config.getInt("Count");
        double conicAngle = this.config.getDouble("ConicAngle");

        for (Vector vector : VectorUtils.getConicVectors(source, conicAngle, count - 1)) {
            Location loc = source.clone().setDirection(vector);
            this.launch(false, loc);
        }

        if (++this.charge == maxPunches) {
            this.reset(true);
        }
    }

    @EventHandler
    public void onAbilityUse(AbilityUseEvent event) {
        if (!(event.getAbility() instanceof DjsPassion)) return;
        if (event.getAbility().getPlayer() != this.player) return;
        if (!this.isPlaced) return;

        Location tpLocation = this.block.getLocation().add(0, 1, 0);

        if (this.player.isSneaking()) {
            tpLocation = this.block.getLocation().subtract(0, 2, 0);
        }

        tpLocation.add(0.5, 0, 0.5);

        boolean solid1 = tpLocation.getBlock().getType().isSolid();
        boolean solid2 = tpLocation.clone().add(0, 1, 0).getBlock().getType().isSolid();

        if (!solid1 && !solid2) {
            this.player.teleport(tpLocation.setDirection(this.player.getEyeLocation().getDirection()));

            this.player.getWorld().playSound(this.player.getLocation(), Sound.NOTE_PLING, 2, 1);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 2);

            ((DjsPassion) event.getAbility()).setSucceeded(true);
        }
    }

    private static class MusicDiscProjectile extends ItemProjectile {

        public MusicDiscProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 2);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.FIREWORKS_SPARK).show(this.entity.getLocation());
        }

        private void showEffect() {
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.entity.getLocation());
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.NOTE_PLING, 2, 2);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.showEffect();
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.showEffect();
        }
    }
}
