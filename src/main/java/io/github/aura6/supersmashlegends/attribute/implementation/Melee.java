package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.Nameable;
import io.github.aura6.supersmashlegends.damage.Damage;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.TeamPreference;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
        if (event.getDamager() != this.player) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (TeamPreference.ENEMY.validate(this.plugin.getTeamManager().getPlayerTeam(this.player), entity)) {
            Section config = this.plugin.getResources().getConfig().getSection("Damage.Melee");

            Location loc = this.player.getEyeLocation();
            loc.setPitch(0);

            Damage damage = Damage.Builder.fromConfig(config, loc.getDirection()).setDamage(this.kit.getDamage()).build();
            this.plugin.getDamageManager().attemptAttributeDamage(entity, damage, this);
        }
    }
}
