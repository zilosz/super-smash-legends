package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ChargedRightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ActualProjectile;
import org.bukkit.Location;
import org.bukkit.entity.Egg;

public class Eggstacy extends ChargedRightClickAbility {

    public Eggstacy(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onChargeTick() {
        EggProjectile eggProjectile = new EggProjectile(plugin, this, config);
        eggProjectile.setSpread(0);
        eggProjectile.launch();

        for (int i = 0; i < config.getInt("ExtraEggsPerTick"); i++) {
            new EggProjectile(plugin, this, config).launch();
        }
    }

    public static class EggProjectile extends ActualProjectile<Egg> {

        public EggProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public Egg createProjectile(Location location) {
            return location.getWorld().spawn(location, Egg.class);
        }
    }
}
