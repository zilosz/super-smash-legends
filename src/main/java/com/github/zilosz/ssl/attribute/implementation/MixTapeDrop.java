package com.github.zilosz.ssl.attribute.implementation;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.attack.AttackType;
import com.github.zilosz.ssl.attribute.RightClickAbility;
import com.github.zilosz.ssl.attack.Attack;
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
        AttackInfo attackInfo = new AttackInfo(AttackType.MIX_TAPE_DROP, this);
        new MixTapeProjectile(this.config.getSection("Projectile"), attackInfo).launch();
        this.player.getWorld().playSound(this.player.getLocation(), Sound.NOTE_SNARE_DRUM, 2, 1);
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(-this.config.getDouble("Recoil")));
    }

    private static class MixTapeProjectile extends ItemProjectile {

        public MixTapeProjectile(Section config, AttackInfo attackInfo) {
            super(config, attackInfo);
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.showEffect();

            EntitySelector selector = new DistanceSelector(this.config.getDouble("Ground.Radius"));
            EntityFinder finder = new EntityFinder(selector);

            finder.findAll(this.launcher, this.entity.getLocation()).forEach(target -> {
                Vector direction = VectorUtils.fromTo(this.entity, target);
                Attack attack = YamlReader.attack(this.config.getSection("Ground"), direction);

                if (SSL.getInstance().getDamageManager().attack(target, attack, this.attackInfo)) {
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
