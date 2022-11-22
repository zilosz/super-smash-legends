package io.github.aura6.supersmashlegends.power.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.BlockProjectile;
import io.github.aura6.supersmashlegends.projectile.CustomProjectile;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Droid extends RightClickAbility {
    private int droidsUsed = 0;

    public Droid(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getDisplayName() {
        return config.getString("Name");
    }

    @Override
    public ItemStack buildItem() {
        ItemStack stack = super.buildItem();
        stack.setAmount(config.getInt("Uses"));
        return stack;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location mainLaunchLoc = player.getEyeLocation();

        player.getWorld().playSound(player.getEyeLocation(), Sound.FIREWORK_LAUNCH, 4, 1);
        Vector sideways = VectorUtils.perp(mainLaunchLoc.getDirection());

        Set<SideDroidProjectile> wings = new HashSet<>();

        Arrays.asList(-2, -1, 1, 2).forEach(sidewaysMultiplier -> {
            SideDroidProjectile wing = new SideDroidProjectile(plugin, this, config.getSection("Side"));
            Location launchLoc = mainLaunchLoc.clone().add(sideways.clone().multiply(sidewaysMultiplier));
            wing.setOverrideLocation(launchLoc);
            wings.add(wing);
        });

        MainDroidProjectile mainDroidProjectile = new MainDroidProjectile(plugin, this, config.getSection("Main"), wings);
        mainDroidProjectile.setOverrideLocation(mainLaunchLoc);
        mainDroidProjectile.launch();

        droidsUsed++;
        player.getInventory().getItem(slot).setAmount(config.getInt("Uses") - droidsUsed);

        if (droidsUsed == config.getInt("Uses")) {
            destroy();
            kit.removeAttribute(this);
        }
    }

    public static class SideDroidProjectile extends BlockProjectile {

        public SideDroidProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }
    }

    public static class MainDroidProjectile extends BlockProjectile {
        private final Set<SideDroidProjectile> wings;
        private LivingEntity homingTarget;

        public MainDroidProjectile(SuperSmashLegends plugin, Ability ability, Section config, Set<SideDroidProjectile> wings) {
            super(plugin, ability, config);
            this.wings = wings;
        }

        @Override
        public FallingBlock createEntity(Location location) {
            FallingBlock entity = super.createEntity(location);
            entity.setCustomName("&8&lDroid");
            return entity;
        }

        @Override
        public void onLaunch() {
            wings.forEach(CustomProjectile::launch);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            new ParticleBuilder(EnumParticle.SMOKE_LARGE).setFace(result.getFace())
                    .boom(this.plugin, this.entity.getLocation(), 7, 0.7, 9);

            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 4, 1);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, this.entity.getLocation(), 7, 0.7, 15);
            this.entity.getWorld().playSound(getEntity().getLocation(), Sound.EXPLODE, 4, 1);
        }

        @Override
        public void onTick() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.FIREWORK_LAUNCH, 3, 2);

            EntityFinder finder = new EntityFinder(this.plugin, new DistanceSelector(config.getDouble("Vision")));

            finder.findClosest(this.launcher, this.entity.getLocation()).ifPresentOrElse(target -> {

                if (this.homingTarget == target) {
                    Location targetLoc = target.getLocation().add(0, EntityUtils.height(target), 0);
                    Vector direction = VectorUtils.fromTo(entity.getLocation(), targetLoc).normalize();
                    this.entity.setVelocity(direction.multiply(config.getDouble("HomingSpeed")));

                    for (int i = 0; i < 5; i++) {
                        new ParticleBuilder(EnumParticle.REDSTONE).show(this.entity.getLocation());
                    }

                } else {
                    this.homingTarget = target;
                    this.hasGravity = true;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.WOLF_HOWL, 3, 2);
                    }
                }

            }, () -> {
                for (int i = 0; i < 4; i++) {
                    new ParticleBuilder(EnumParticle.SMOKE_LARGE).show(this.entity.getLocation());
                }
            });

            this.wings.forEach(wing -> wing.setVelocity(this.entity.getVelocity().add(new Vector(0, 0.035, 0))));
        }

        @Override
        public void onRemove() {
            this.wings.forEach(SideDroidProjectile::remove);
        }

        @EventHandler
        public void onDeath(EntityDeathEvent event) {
            if (event.getEntity() == this.homingTarget) {
                this.homingTarget = null;
                this.hasGravity = false;
            }
        }
    }
}
