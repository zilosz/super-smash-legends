package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.ClickableAbility;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.LivingProjectile;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class BatWave extends RightClickAbility {
    private MainBatProjectile mainBatProjectile;
    private Set<BatProjectile> batProjectiles;

    public BatWave(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void addAndLaunch(BatProjectile bat) {
        batProjectiles.add(bat);
        bat.launch();
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

        if (mainBatProjectile == null || mainBatProjectile.state == BatWaveState.INACTIVE) {
            sendUseMessage();

            batProjectiles = new HashSet<>();
            mainBatProjectile = new MainBatProjectile(plugin, this, config);
            addAndLaunch(mainBatProjectile);

            for (int i = 1; i < config.getInt("BatCount"); i++) {
                addAndLaunch(new BatProjectile(plugin, this, config));
            }

        } else if (mainBatProjectile.state == BatWaveState.UNLEASHED) {
            batProjectiles.forEach(BatProjectile::leash);

        } else {
            batProjectiles.forEach(BatProjectile::unleash);
        }
    }

    public enum BatWaveState {
        INACTIVE,
        LEASHED,
        UNLEASHED
    }

    public static class BatProjectile extends LivingProjectile<Bat> {

        public BatProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public Bat createEntity(Location location) {
            Bat bat = location.getWorld().spawn(location, Bat.class);
            bat.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10_000, 10_000));
            return bat;
        }

        public void leash() {
            this.entity.setLeashHolder(this.launcher);
        }

        public void unleash() {
            this.entity.setLeashHolder(null);
        }

        @Override
        public void onRemove() {
            this.unleash();
        }
    }

    public static class MainBatProjectile extends BatProjectile {
        private BatWaveState state = BatWaveState.INACTIVE;

        public MainBatProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onLaunch() {
            this.state = BatWaveState.UNLEASHED;
        }

        @Override
        public void leash() {
            super.leash();
            this.state = BatWaveState.LEASHED;
        }

        @Override
        public void unleash() {
            super.unleash();
            this.state = BatWaveState.UNLEASHED;
        }

        @Override
        public void onTick() {
            if (this.state == BatWaveState.LEASHED) {
                this.launcher.setVelocity(this.constantVelocity);
            }
        }

        @Override
        public void onRemove() {
            this.state = BatWaveState.INACTIVE;
            ((ClickableAbility) this.ability).startCooldown();
        }
    }
}
