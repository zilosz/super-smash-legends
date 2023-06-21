package com.github.zilosz.ssl.damage;

import com.github.zilosz.ssl.Resources;
import com.github.zilosz.ssl.SSL;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

@Getter
public class AttackSettings {
    private final DamageSettings damageSettings;
    private final KbSettings kbSettings;
    private int immunityTicks;

    public AttackSettings(DamageSettings damageSettings, KbSettings kbSettings, int immunityTicks) {
        this.damageSettings = damageSettings;
        this.kbSettings = kbSettings;
        this.immunityTicks = immunityTicks;
    }

    public AttackSettings(Section config, Vector direction) {
        this.damageSettings = new DamageSettings(config);
        this.kbSettings = new KbSettings(direction, config);

        Resources resources = SSL.getInstance().getResources();
        int defaultImmunity = resources.getConfig().getInt("Damage.DefaultImmunityTicks");
        this.immunityTicks = config.getOptionalInt("ImmunityTicks").orElse(defaultImmunity);
    }

    public AttackSettings modifyDamage(Consumer<DamageSettings> consumer) {
        consumer.accept(this.damageSettings);
        return this;
    }

    public AttackSettings modifyKb(Consumer<KbSettings> consumer) {
        consumer.accept(this.kbSettings);
        return this;
    }

    public AttackSettings setImmunityTicks(int immunityTicks) {
        this.immunityTicks = immunityTicks;
        return this;
    }
}
