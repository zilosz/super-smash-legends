package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.event.AttributeDamageEvent;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import io.github.aura6.supersmashlegends.utils.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.finder.range.HitBoxSelector;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Thunderbolt extends ChargedRightClickAbility {

    public Thunderbolt(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 1, 2);
    }

    private void endEffect(Location location) {
        location.getWorld().strikeLightningEffect(location);
        location.getWorld().playSound(location, Sound.AMBIENCE_THUNDER, 4, 0.5f);
        new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).boom(plugin, location, 5, 0.5, 18);
    }

    @Override
    public void onSuccessfulCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 2, 0.5f);

        EntityFinder finder = new EntityFinder(plugin, new HitBoxSelector(config.getDouble("HitBox")));

        double damage = YamlReader.incLin(config, "Damage", ticksCharging, maxChargeTicks);
        double kb = YamlReader.incLin(config, "Kb", ticksCharging, maxChargeTicks);
        double range = YamlReader.incLin(config, "Range", ticksCharging, maxChargeTicks);

        Location location = player.getEyeLocation();
        Vector step = location.getDirection().multiply(0.25);

        boolean found = false;
        double stepped = 0;

        while (true) {

            if (stepped > range || location.getBlock().getType().isSolid() || found) {
                endEffect(location);
                break;
            }

            for (LivingEntity target : finder.findAll(player, location)) {
                Damage dmg = Damage.Builder.fromConfig(config, step).setDamage(damage).setKb(kb).build();

                if (plugin.getDamageManager().attemptAttributeDamage(new AttributeDamageEvent(target, dmg, this))) {
                    found = true;
                    break;
                }
            }

            new ParticleBuilder(EnumParticle.REDSTONE).setRgb(255, 255, 0).show(location);

            stepped += 0.25;
            location.add(step);
        }
    }

    @Override
    public void onFailedCharge() {
        player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1, 2);
    }
}
