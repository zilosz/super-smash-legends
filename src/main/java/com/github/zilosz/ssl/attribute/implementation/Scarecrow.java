package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Damage;
import com.github.zilosz.ssl.damage.KnockBack;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.event.attack.AttackEvent;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import com.github.zilosz.ssl.utils.block.MaterialInfo;
import com.github.zilosz.ssl.utils.collection.CollectionUtils;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.EntityUtils;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.message.Chat;
import com.github.zilosz.ssl.utils.message.MessageUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class Scarecrow extends RightClickAbility {
    private final Map<Block, MaterialInfo> plantBlocks = new HashMap<>();
    private final Set<LivingEntity> affectedTargets = new HashSet<>();
    private NPC scarecrow;
    private int ticksUntilCanTp;
    private int ticksLeft;
    private BukkitTask plantTask;
    private BukkitTask removeTask;
    private BukkitTask mainTask;
    private Hologram npcHologram;
    private TextHologramLine tpHintLine;

    private String getTpHint(boolean canTp) {
        return MessageUtils.color(this.config.getString("Hologram.TpHint." + (canTp ? "Yes" : "No")));
    }

    private Location getHologramLocation() {
        return EntityUtils.top(this.scarecrow.getEntity()).add(0, this.config.getDouble("Hologram.HeightAboveHead"), 0);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (this.scarecrow != null && this.scarecrow.isSpawned()) {

            if (this.ticksUntilCanTp == 0) {
                this.tpHintLine.setText(this.getTpHint(false));

                Location playerLoc = this.player.getLocation();
                this.player.teleport(this.scarecrow.getStoredLocation());
                this.player.playSound(this.player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0.75f);

                this.scarecrow.getEntity().teleport(playerLoc);
                playerLoc.getWorld().playSound(playerLoc, Sound.ENDERMAN_TELEPORT, 1, 2);

                this.ticksUntilCanTp = this.config.getInt("ActiveTpDelay");

                this.unPlant();
                this.startPlantTask();

            } else {
                String secUntilTp = new DecimalFormat("#.#").format(this.ticksUntilCanTp / 20.0);
                Chat.ABILITY.send(this.player, String.format("&7You can teleport in &e%s &7seconds.", secUntilTp));
            }

        } else {
            this.sendUseMessage();

            this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_SPAWN, 0.5f, 2);
            this.player.getWorld().playSound(this.player.getLocation(), Sound.DIG_GRAVEL, 1, 0.5f);

            this.scarecrow = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "");
            SSL.getInstance().getNpcStorage().addNpc(this.scarecrow);

            SkinTrait skinTrait = this.scarecrow.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName(this.kit.getSkinName());

            LookClose lookClose = this.scarecrow.getOrAddTrait(LookClose.class);
            lookClose.setRange(this.getEffectRange());

            Location eyeLoc = this.player.getEyeLocation();
            Vector direction = eyeLoc.getDirection();

            Location spawnLoc = eyeLoc.clone().add(direction.clone().multiply(this.config.getDouble("EyeDistance")));
            spawnLoc.setPitch(0);
            this.scarecrow.spawn(spawnLoc);

            this.npcHologram = HolographicDisplaysAPI.get(SSL.getInstance()).createHologram(this.getHologramLocation());
            this.npcHologram.getLines().appendText(MessageUtils.color(this.config.getString("Hologram.Title")));
            this.tpHintLine = this.npcHologram.getLines().appendText(this.getTpHint(false));

            VisibilitySettings settings = this.npcHologram.getVisibilitySettings();
            settings.setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            settings.setIndividualVisibility(this.player, VisibilitySettings.Visibility.VISIBLE);

            this.scarecrow.getEntity().setVelocity(direction.multiply(this.config.getDouble("LaunchVelocity")));

            int lifetime = this.config.getInt("CrowLifetime");
            this.ticksLeft = lifetime;

            this.removeTask = Bukkit.getScheduler().runTaskLater(SSL.getInstance(), () -> {
                this.reset();
                this.startCooldown();
            }, lifetime);

            this.ticksUntilCanTp = this.config.getInt("InitialTpDelay");
            EntitySelector selector = new DistanceSelector(this.getEffectRange());

            this.mainTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
                this.npcHologram.setPosition(this.getHologramLocation());

                if (this.ticksUntilCanTp > 0 && --this.ticksUntilCanTp == 0) {
                    this.player.playSound(this.player.getLocation(), Sound.ORB_PICKUP, 1, 0.5f);
                    this.tpHintLine.setText(this.getTpHint(true));
                }

                this.player.setExp((float) --this.ticksLeft / lifetime);

                Location location = this.scarecrow.getStoredLocation();

                Predicate<LivingEntity> predicate = entity -> !selector.containsEntity(location, entity);
                CollectionUtils.removeWhileIterating(this.affectedTargets, this::removeEffect, predicate);

                EntityFinder finder = new EntityFinder(selector).avoidAll(this.affectedTargets);

                finder.findAll(this.player, location).forEach(target -> {
                    target.getWorld().playSound(target.getLocation(), Sound.DIG_GRASS, 1.2f, 0.8f);
                    this.player.playSound(this.player.getLocation(), Sound.ENDERMAN_HIT, 1, 2);

                    Section slowConfig = this.config.getSection("Slowness");
                    PotionEffect effect = YamlReader.getPotionEffect(slowConfig, PotionEffectType.SLOW);
                    PotionEffectEvent.fromPotionEffect(target, effect).apply();

                    target.setMetadata("pumpkin", new FixedMetadataValue(SSL.getInstance(), ""));
                    this.affectedTargets.add(target);

                    if (target instanceof Player) {
                        Player targetPlayer = (Player) target;
                        targetPlayer.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));

                    } else {
                        target.setPassenger(BlockUtils.spawnFallingBlock(target.getLocation(), Material.PUMPKIN));
                    }
                });
            }, 1, 0);

            this.startPlantTask();
        }
    }

    private void unPlant() {

        if (this.plantTask != null) {
            this.plantTask.cancel();
        }

        CollectionUtils.removeWhileIteratingOverEntry(this.plantBlocks, (block, info) -> {
            this.player.getWorld().playSound(this.scarecrow.getStoredLocation(), Sound.DIG_SAND, 1, 1);
            info.apply(block);
        });
    }

    private void startPlantTask() {

        this.plantTask = Bukkit.getScheduler().runTaskTimer(SSL.getInstance(), () -> {
            Location crowLoc = this.scarecrow.getStoredLocation();

            if (this.player.getLocation().getY() - crowLoc.getY() > this.config.getDouble("MinYDistanceFromPlayer")) {
                this.reset();
                this.startCooldown();
                return;
            }

            if (!this.scarecrow.getEntity().isOnGround()) return;

            this.plantTask.cancel();

            this.scarecrow.getEntity().setVelocity(new Vector(0, 0, 0));
            this.scarecrow.getEntity().teleport((crowLoc.getBlock().getLocation().add(0.5, 0, 0.5)));

            this.player.getWorld().playSound(crowLoc, Sound.DIG_GRASS, 2, 0.8f);

            for (int xChange = -1; xChange <= 1; xChange++) {

                for (int zChange = -1; zChange <= 1; zChange++) {
                    Location topLoc = crowLoc.clone().add(xChange, 0, zChange);
                    Block topBlock = topLoc.getBlock();

                    Location underLoc = topLoc.clone().subtract(0, 1, 0);
                    Block underBlock = underLoc.getBlock();

                    if (underBlock.getType().isSolid() && !topBlock.getType().isSolid()) {
                        this.plantBlocks.put(underBlock, MaterialInfo.fromBlock(underBlock));
                        underBlock.setType(Material.GRASS);

                        if (!topBlock.equals(crowLoc.getBlock())) {
                            this.plantBlocks.put(topBlock, MaterialInfo.fromBlock(topBlock));
                            topBlock.setType(Material.LONG_GRASS);
                            topBlock.setData((byte) 1);
                        }
                    }
                }
            }
        }, 3, 0);
    }

    private double getEffectRange() {
        return this.config.getDouble("EffectRange");
    }

    private void reset() {
        if (this.mainTask == null) return;

        this.player.setExp(0);

        this.removeTask.cancel();
        this.mainTask.cancel();

        Optional.ofNullable(this.scarecrow.getEntity()).ifPresent(entity -> {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.SMOKE_LARGE);
            new ParticleMaker(particle).solidSphere(entity.getLocation(), 0.75, 15, 0.25);
            entity.getWorld().playSound(entity.getLocation(), Sound.WITHER_HURT, 1, 0.5f);
            this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 2);
        });

        this.npcHologram.delete();

        this.scarecrow.destroy();
        SSL.getInstance().getNpcStorage().removeNpc(this.scarecrow);
        CollectionUtils.removeWhileIterating(this.affectedTargets, this::removeEffect);

        this.unPlant();
    }

    private void removeEffect(LivingEntity entity) {
        entity.removePotionEffect(PotionEffectType.SLOW);
        entity.removeMetadata("pumpkin", SSL.getInstance());

        if (entity instanceof Player) {
            ((Player) entity).getInventory().setHelmet(new ItemStack(Material.AIR));

        } else {
            Optional.ofNullable(entity.getPassenger()).ifPresent(Entity::remove);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.reset();
    }

    @EventHandler
    public void onMelee(AttackEvent event) {
        if (event.getAttribute().getPlayer() != this.player) return;
        if (!(event.getAttribute() instanceof Melee)) return;
        if (!this.affectedTargets.contains(event.getVictim())) return;

        Section boostConfig = this.config.getSection("MeleePumpkinBoost");

        Damage damage = event.getAttack().getDamage();
        damage.setDamage(damage.getDamage() * boostConfig.getDouble("Damage"));

        KnockBack kb = event.getAttack().getKb();
        kb.setKb(kb.getKb() * boostConfig.getDouble("Kb"));

        event.getVictim().getWorld().playSound(event.getVictim().getLocation(), Sound.WITHER_HURT, 1, 1);
    }
}
