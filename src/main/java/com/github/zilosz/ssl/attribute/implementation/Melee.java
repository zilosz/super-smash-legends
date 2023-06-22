package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.Nameable;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamPreference;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class Melee extends Attribute implements Nameable {

    public Melee(SSL plugin, Kit kit) {
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
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        event.setCancelled(true);

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        LivingEntity victim = (LivingEntity) event.getEntity();

        Team team = this.plugin.getTeamManager().getPlayerTeam(this.player);
        if (TeamPreference.FRIENDLY.validate(team, victim)) return;

        Section config = this.plugin.getResources().getConfig().getSection("Damage.Melee");
        Vector direction = this.player.getEyeLocation().getDirection();
        double damage = this.plugin.getKitManager().getSelectedKit(this.player).getDamage();

        AttackSettings settings = new AttackSettings(config, direction)
                .modifyDamage(damageSettings -> damageSettings.setDamage(damage));

        this.plugin.getDamageManager().attack(victim, this, settings);
    }
}
