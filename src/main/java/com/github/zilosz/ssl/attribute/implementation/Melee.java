package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.attribute.Nameable;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.utils.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Melee extends Attribute implements Nameable {

    @Override
    public String getDisplayName() {
        return "&cMelee";
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() != this.player) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        event.setCancelled(true);

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            LivingEntity victim = (LivingEntity) event.getEntity();
            Team team = SSL.getInstance().getTeamManager().getPlayerTeam(this.player);

            if (TeamPreference.ENEMY.validate(team, victim)) {
                SSL.getInstance().getDamageManager().attack(victim, this, this.createAttack(this.player));

            }
        }
    }

    public Attack createAttack(LivingEntity user) {
        Section config = SSL.getInstance().getResources().getConfig().getSection("Damage.Melee");
        double damage = SSL.getInstance().getKitManager().getSelectedKit(this.player).getDamage();

        Attack attack = YamlReader.attack(config, user.getEyeLocation().getDirection());
        attack.getDamage().setDamage(damage);

        return attack;
    }
}
