package io.github.aura6.supersmashlegends.event.damage;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.Resources;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Attribute;
import io.github.aura6.supersmashlegends.damage.KbSettings;
import io.github.aura6.supersmashlegends.kit.KitManager;
import io.github.aura6.supersmashlegends.utils.file.YamlReader;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Optional;

public class KbEvent extends SingleAttackEvent {
    @Getter private final KbSettings kbSettings;

    public KbEvent(LivingEntity victim, Attribute attribute, KbSettings kbSettings) {
        super(victim, attribute);
        this.kbSettings = kbSettings;
    }

    public double getFinalKb() {
        double kb = this.kbSettings.getKb();

        if (this.kbSettings.isFactorsHealth()) {
            Resources resources = SuperSmashLegends.getInstance().getResources();
            Section config = resources.getConfig().getSection("Damage");
            kb *= YamlReader.decLin(config, "KbHealthMultiplier", victim.getHealth(), victim.getMaxHealth());
        }

        if (this.kbSettings.isFactorsKit() && this.victim instanceof Player) {
            KitManager kitManager = SuperSmashLegends.getInstance().getKitManager();
            kb *= kitManager.getSelectedKit((Player) this.victim).getKb();
        }

        return kb;
    }

    public Optional<Vector> getFinalKbVector() {
        Vector direction = this.kbSettings.getDirection();

        if (direction == null) {
            return Optional.empty();
        }

        double finalKb = this.getFinalKb();
        Vector velocity = direction.clone().setY(0).normalize().multiply(new Vector(finalKb, 1, finalKb));
        velocity.setY(this.kbSettings.isLinear() ? direction.getY() : this.kbSettings.getKbY());

        if (this.kbSettings.isFactorsPreviousVelocity()) {
            velocity = this.victim.getVelocity().add(velocity);
        }

        return Optional.of(velocity);
    }
}
