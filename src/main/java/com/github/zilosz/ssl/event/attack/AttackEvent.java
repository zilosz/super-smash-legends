package com.github.zilosz.ssl.event.attack;

import com.github.zilosz.ssl.attack.Attack;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.Damage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;

@Getter
public class AttackEvent extends AbstractDamageEvent {
    private final Attack attack;
    private final AttackInfo attackInfo;
    @Setter private boolean cancelled = false;

    public AttackEvent(LivingEntity victim, Attack attack, AttackInfo attackInfo) {
        super(victim);
        this.attack = attack;
        this.attackInfo = attackInfo;
    }

    @Override
    public Damage getDamage() {
        return this.attack.getDamage();
    }
}

