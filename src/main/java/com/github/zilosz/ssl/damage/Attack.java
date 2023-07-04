package com.github.zilosz.ssl.damage;

import com.github.zilosz.ssl.Resources;
import com.github.zilosz.ssl.SSL;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

@Getter
public class Attack {
    private final Damage damage;
    private final KnockBack kb;
    private int immunityTicks;

    public Attack(Damage damage, KnockBack kb, int immunityTicks) {
        this.damage = damage;
        this.kb = kb;
        this.immunityTicks = immunityTicks;
    }

    public Attack(Section config) {
        this(config, null);
    }

    public Attack(Section config, Vector direction) {
        this.damage = new Damage(config);
        this.kb = new KnockBack(direction, config);

        Resources resources = SSL.getInstance().getResources();
        int defaultImmunity = resources.getConfig().getInt("Damage.DefaultImmunityTicks");
        this.immunityTicks = config.getOptionalInt("ImmunityTicks").orElse(defaultImmunity);
    }

    public Attack modifyDamage(Consumer<Damage> consumer) {
        consumer.accept(this.damage);
        return this;
    }

    public Attack modifyKb(Consumer<KnockBack> consumer) {
        consumer.accept(this.kb);
        return this;
    }

    public Attack setImmunityTicks(int immunityTicks) {
        this.immunityTicks = immunityTicks;
        return this;
    }
}
