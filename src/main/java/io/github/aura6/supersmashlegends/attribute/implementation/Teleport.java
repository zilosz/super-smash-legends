package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.block.BlockRay;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

public class Teleport extends RightClickAbility {

    public Teleport(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location eyeLoc = player.getEyeLocation();

        BlockRay blockRay = new BlockRay(eyeLoc, eyeLoc.getDirection());
        blockRay.cast(config.getInt("Range"));
        player.teleport(blockRay.getEmptyDestination());

        player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 1, 2);
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(eyeLoc, 1.1, 10, 0.3);
    }
}
