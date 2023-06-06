package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.PassiveAbility;
import io.github.aura6.supersmashlegends.event.RegenEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Bloodlust extends PassiveAbility {

    public Bloodlust(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public String getUseType() {
        return "Melee";
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() != this.player || this.player.getHealth() >= 20) return;
        if (!RegenEvent.attempt(this.player, this.config.getDouble("Regen"))) return;

        this.player.getWorld().playSound(this.player.getLocation(), Sound.ZOMBIE_UNFECT, 1, 2);
        new ParticleBuilder(EnumParticle.REDSTONE).boom(this.plugin, EntityUtils.center(event.getEntity()), 3, 0.3, 7);
    }
}
