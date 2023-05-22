package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.Nameable;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class Melee extends Attribute implements Nameable {

    public Melee(SuperSmashLegends plugin, Kit kit) {
        super(plugin, kit);
    }

    @Override
    public String getDisplayName() {
        return "&cMelee";
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() != player) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (TeamPreference.FRIENDLY.validate(plugin.getTeamManager().getPlayerTeam(player), entity)) return;

        Section config = plugin.getResources().getConfig().getSection("Damage.Melee");
        Vector direction = player.getEyeLocation().getDirection();
        Damage damage = Damage.Builder.fromConfig(config, direction).setDamage(kit.getDamage()).build();

        plugin.getDamageManager().attemptAttributeDamage(entity, damage, this);
    }
}