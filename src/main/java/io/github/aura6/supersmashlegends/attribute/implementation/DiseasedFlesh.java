package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.CollectionUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class DiseasedFlesh extends RightClickAbility {

    private static final List<Material> ITEMS = Arrays.asList(
            Material.ROTTEN_FLESH,
            Material.ROTTEN_FLESH,
            Material.SAND,
            Material.SPIDER_EYE
    );

    public DiseasedFlesh(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        player.getWorld().playSound(player.getEyeLocation(), Sound.ZOMBIE_HURT, 1, 1);

        FleshProjectile projectile = new FleshProjectile(plugin, this, config);
        projectile.setSpread(0);
        projectile.launch();

        for (int i = 1; i < config.getInt("Count"); i++) {
            new FleshProjectile(plugin, this, config).launch();
        }
    }

    public static class FleshProjectile extends ItemProjectile {

        public FleshProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public ItemStack getStack() {
            return new ItemStack(CollectionUtils.selectRandom(ITEMS));
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 1));
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(165, 42, 42).show(this.entity.getLocation());
        }
    }
}
