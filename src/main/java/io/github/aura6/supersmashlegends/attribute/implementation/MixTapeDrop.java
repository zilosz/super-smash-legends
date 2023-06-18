package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.damage.AttackSettings;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.finder.EntityFinder;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.DistanceSelector;
import io.github.aura6.supersmashlegends.utils.entity.finder.selector.EntitySelector;
import io.github.aura6.supersmashlegends.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class MixTapeDrop extends RightClickAbility {

    public MixTapeDrop(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new MixTapeProjectile(this.plugin, this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 1);
    }

    public static class MixTapeProjectile extends ItemProjectile {

        public MixTapeProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            new ParticleBuilder(EnumParticle.NOTE).show(this.entity.getLocation());
        }

        private void showEffect() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 2, 1);
            new ParticleBuilder(EnumParticle.EXPLOSION_LARGE).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.showEffect();
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.showEffect();

            EntitySelector selector = new DistanceSelector(this.config.getDouble("Ground.Radius"));
            EntityFinder finder = new EntityFinder(this.plugin, selector);
            Player player = this.ability.getPlayer();

            finder.findAll(player, this.entity.getLocation()).forEach(target -> {
                Vector direction = VectorUtils.fromTo(this.entity, target);
                AttackSettings settings = new AttackSettings(this.config.getSection("Ground"), direction);

                if (this.plugin.getDamageManager().attack(target, this.ability, settings)) {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 2, 2);
                }
            });
        }
    }
}
