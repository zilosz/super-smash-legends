package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.AttackSettings;
import io.github.aura6.supersmashlegends.event.attack.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.event.attribute.AbilityUseEvent;
import io.github.aura6.supersmashlegends.event.projectile.ProjectileHitBlockEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.block.BlockRay;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.EntitySelector;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import io.github.aura6.supersmashlegends.utils.message.Chat;
import io.github.aura6.supersmashlegends.utils.message.MessageUtils;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class Boombox extends RightClickAbility {
    private boolean isPlaced = false;
    private BukkitTask allowClickTask;
    private int clickTicksLeft = 0;
    private Block block;
    private ArmorStand stand;
    private double health;
    private int charge = 0;
    private BukkitTask healthTask;
    private Hologram hologram;

    public Boombox(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.isPlaced;
    }

    private void reset() {
        if (!this.isPlaced) return;

        this.startCooldown();

        this.isPlaced = false;
        this.charge = 0;

        this.block.setType(Material.AIR);
        this.stand.remove();
        this.hologram.delete();

        this.healthTask.cancel();

        this.allowClickTask.cancel();
        this.clickTicksLeft = 0;

        this.player.getWorld().playSound(this.block.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 0.8f);
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }

    private double getMaxHealth() {
        return this.config.getDouble("Health");
    }

    private void updateHealth(double loss) {
        this.health -= loss;

        if (this.health <= 0) {
            this.reset();
            return;
        }

        String bar = MessageUtils.progressBar("|", "|", "&a", "&c", this.health, this.getMaxHealth(), 20);
        this.stand.setCustomName(bar);
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

        Location above = this.block.getLocation().add(0.5, 1, 0.5);

        this.stand = this.player.getWorld().spawn(above.clone().subtract(0, 0.8, 0), ArmorStand.class);
        this.stand.setCustomNameVisible(true);
        this.stand.setGravity(false);
        this.stand.setVisible(false);
        this.stand.setSmall(true);

        this.health = this.getMaxHealth();
        this.updateHealth(0);

        double healthLossPerTick = this.health / this.config.getInt("MaxDuration");

        this.healthTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> this.updateHealth(healthLossPerTick), 1, 1);

        this.hologram = HolographicDisplaysAPI.get(this.plugin).createHologram(above.add(0, 0.4, 0));
        String color = this.kit.getColor().getChatSymbol();
        String title = String.format("&f%s's %s&lBoombox", this.player.getName(), color);
        this.hologram.getLines().appendText(MessageUtils.color(title));
    }

    private void explode() {
        this.reset();

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
    }

    @EventHandler
    public void onDamage(AttributeDamageEvent event) {
        if (event.getVictim() != this.stand) return;

        event.setCancelled(true);

        if (event.getAttribute() instanceof MixTapeDrop) {
            this.explode();

        } else if (event.getAttribute().getPlayer() != this.player) {
            this.updateHealth(event.getFinalDamage());
            this.player.getWorld().playSound(this.stand.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 2);
        }
    }

    @EventHandler
    public void onProjectileHitBlock(ProjectileHitBlockEvent event) {
        if (!this.isPlaced) return;
        if (!(event.getResult().getBlock().equals(this.block))) return;
        if (!(event.getProjectile() instanceof MixTapeDrop.MixTapeProjectile)) return;

        this.explode();
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
        if (event.getPlayer() != this.player) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().equals(this.block)) return;

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
            this.reset();
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

        public MusicDiscProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
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
