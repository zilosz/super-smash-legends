package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.damage.Attack;
import com.github.zilosz.ssl.projectile.ItemProjectile;
import com.github.zilosz.ssl.utils.block.BlockHitResult;
import com.github.zilosz.ssl.utils.effects.ParticleMaker;
import com.github.zilosz.ssl.utils.entity.finder.EntityFinder;
import com.github.zilosz.ssl.utils.entity.finder.selector.EntitySelector;
import com.github.zilosz.ssl.utils.entity.finder.selector.implementation.DistanceSelector;
import com.github.zilosz.ssl.utils.file.YamlReader;
import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.NoteColor;

public class MixTapeDrop extends RightClickAbility {

    @Override
    public void onClick(PlayerInteractEvent event) {
        new MixTapeProjectile(this, this.config.getSection("Projectile")).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 1);
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(-this.config.getDouble("Recoil")));
    }

    private static class MixTapeProjectile extends ItemProjectile {

        public MixTapeProjectile(Ability ability, Section config) {
            super(ability, config);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.showEffect();

            EntitySelector selector = new DistanceSelector(this.config.getDouble("Ground.Radius"));
            EntityFinder finder = new EntityFinder(selector);

            finder.findAll(this.launcher, this.entity.getLocation()).forEach(target -> {
                Vector direction = VectorUtils.fromTo(this.entity, target);
                Attack attack = YamlReader.attack(this.config.getSection("Ground"), direction);

                if (SSL.getInstance().getDamageManager().attack(target, this.ability, attack)) {
                    this.launcher.playSound(this.launcher.getLocation(), Sound.NOTE_PLING, 2, 2);
                }
            });
        }

        @Override
        public void onTick() {
            ParticleBuilder particle = new ParticleBuilder(ParticleEffect.NOTE)
                    .setParticleData(new NoteColor((int) MathUtils.randRange(0, 25)));
            new ParticleMaker(particle).show(this.entity.getLocation());
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.showEffect();
        }

        private void showEffect() {
            this.entity.getWorld().playSound(this.entity.getLocation(), Sound.EXPLODE, 2, 1);
            new ParticleMaker(new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE)).show(this.entity.getLocation());
        }
    }
}
