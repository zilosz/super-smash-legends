package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.CollectionUtils;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
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

    public DiseasedFlesh(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getEyeLocation(), Sound.ZOMBIE_HURT, 1, 1);

        FleshProjectile projectile = new FleshProjectile(this.plugin, this, this.config);
        projectile.setSpread(0);
        projectile.launch();

        for (int i = 1; i < this.config.getInt("Count"); i++) {
            new FleshProjectile(this.plugin, this, this.config).launch();
        }
    }

    public static class FleshProjectile extends ItemProjectile {

        public FleshProjectile(SSL plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public ItemStack getStack() {
            return new ItemStack(CollectionUtils.selectRandom(ITEMS));
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(165, 42, 42).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 1));
        }
    }
}
