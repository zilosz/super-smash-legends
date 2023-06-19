package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.AttackSettings;
import com.github.zilosz.ssl.utils.effect.ParticleBuilder;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.DistanceSelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.kit.Kit;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class MixTapeDrop extends RightClickAbility {

    public MixTapeDrop(SSL plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        new MixTapeProjectile(this.plugin, this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 1);
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(-this.config.getDouble("Recoil")));
    }

    public static class MixTapeProjectile extends ItemProjectile {

        public MixTapeProjectile(SSL plugin, Ability ability, Section config) {
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
