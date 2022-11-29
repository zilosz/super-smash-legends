package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.EntityUtils;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.DistanceSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShadowAmbush extends RightClickAbility {

    public ShadowAmbush(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void teleport(LivingEntity target) {
        Location targetLoc = target.getLocation();
        Vector targetDir = targetLoc.getDirection();
        Location spotBehind = targetLoc.subtract(targetDir).setDirection(targetDir);

        Block one = spotBehind.getBlock();
        Block two = spotBehind.add(0, 1, 0).getBlock();

        player.teleport(one.getType().isSolid() || two.getType().isSolid() ? targetLoc : spotBehind);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, config.getInt("BlindnessDuration"), 1));

        player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 1, 0.5f);
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(player), 2.5, 15, 0.5);
    }

    private void fail() {
        player.playSound(player.getLocation(), Sound.WITHER_HURT, 1, 2f);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(EntityUtils.center(player), 1.5, 15, 0.5);
        EntityFinder finder = new EntityFinder(plugin, new DistanceSelector(config.getDouble("Range")));
        finder.findClosest(player).ifPresentOrElse(this::teleport, this::fail);
    }
}
