package com.github.zilosz.ssl.attribute.impl;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.Attribute;
import com.github.zilosz.ssl.team.Team;
import com.github.zilosz.ssl.team.TeamPreference;
import com.github.zilosz.ssl.util.file.YamlReader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Melee extends Attribute {

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() != player) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;

    event.setCancelled(true);

    if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
      LivingEntity victim = (LivingEntity) event.getEntity();
      Team team = SSL.getInstance().getTeamManager().getEntityTeam(player);

      if (TeamPreference.ENEMY.validate(team, victim)) {
        AttackInfo attackInfo = new AttackInfo(AttackType.MELEE, this);
        SSL.getInstance().getDamageManager().attack(victim, createAttack(player), attackInfo);

      }
    }
  }

  public Attack createAttack(LivingEntity user) {
    Section config = SSL.getInstance().getResources().getConfig().getSection("Damage.Melee");
    double damage = SSL.getInstance().getKitManager().getSelectedKit(player).getDamage();

    Attack attack = YamlReader.attack(config, user.getEyeLocation().getDirection(), "");
    attack.getDamage().setDamage(damage);

    return attack;
  }
}
