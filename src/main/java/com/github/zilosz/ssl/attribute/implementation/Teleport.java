package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.utils.block.BlockRay;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

public class Teleport extends RightClickAbility {

    public Teleport(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        Location eyeLoc = this.player.getEyeLocation();

        BlockRay blockRay = new BlockRay(eyeLoc, eyeLoc.getDirection());
        blockRay.cast(this.config.getInt("Range"));
        this.player.teleport(blockRay.getEmptyDestination());

        this.player.getWorld().playSound(this.player.getLocation(), Sound.WITHER_HURT, 1, 2);
        new ParticleBuilder(EnumParticle.SMOKE_LARGE).solidSphere(eyeLoc, 1.1, 10, 0.3);
    }
}
