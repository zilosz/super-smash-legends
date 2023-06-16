package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.attribute.Nameable;
import io.github.aura6.supersmashlegends.damage.AttackSettings;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.team.Team;
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
        if (event.getDamager() != this.player) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);
        LivingEntity victim = (LivingEntity) event.getEntity();

        Team team = this.plugin.getTeamManager().getPlayerTeam(this.player);
        if (TeamPreference.FRIENDLY.validate(team, victim)) return;

        Section config = this.plugin.getResources().getConfig().getSection("Damage.Melee");
        Vector direction = this.player.getEyeLocation().getDirection();
        double damage = this.plugin.getKitManager().getSelectedKit(this.player).getDamage();

        this.plugin.getDamageManager().attack(victim, this, new AttackSettings(config, direction)
                .modifyDamage(damageSettings -> damageSettings.setDamage(damage)));
    }
}
