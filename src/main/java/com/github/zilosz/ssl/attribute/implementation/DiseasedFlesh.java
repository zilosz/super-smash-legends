package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.event.PotionEffectEvent;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.collection.RandomCollection;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class DiseasedFlesh extends RightClickAbility {
    private RandomCollection<ItemStack> items;

    @Override
    public void activate() {
        super.activate();
        this.items = new RandomCollection<>();

        for (Section section : YamlReader.getSections(this.config.getSection("Items"))) {
            this.items.add(YamlReader.getStack(section), section.getDouble("Weight"));
        }
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.launch(true);

        for (int i = 1; i < this.config.getInt("Count"); i++) {
            this.launch(false);
        }

        this.player.getWorld().playSound(this.player.getEyeLocation(), Sound.ZOMBIE_PIG_HURT, 1, 2);

    }

    private void launch(boolean first) {
        FleshProjectile projectile = new FleshProjectile(this, this.config);
        projectile.setItemStack(this.items.next());

        if (first) {
            projectile.setSpread(0);
        }

        projectile.launch();
    }

    private static class FleshProjectile extends ItemProjectile {

        public FleshProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(165, 42, 42).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            new PotionEffectEvent(target, PotionEffectType.BLINDNESS, 15, 1).apply();
            new PotionEffectEvent(target, PotionEffectType.WITHER, 15, 1).apply();
        }
    }
}
