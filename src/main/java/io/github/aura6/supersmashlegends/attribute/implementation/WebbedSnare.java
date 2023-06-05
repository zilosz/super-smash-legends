package io.github.aura6.supersmashlegends.attribute.implementation;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import io.github.aura6.supersmashlegends.attribute.RightClickAbility;
import io.github.aura6.supersmashlegends.kit.Kit;
import io.github.aura6.supersmashlegends.projectile.ItemProjectile;
import io.github.aura6.supersmashlegends.utils.block.BlockHitResult;
import io.github.aura6.supersmashlegends.utils.effect.ParticleBuilder;
import io.github.aura6.supersmashlegends.utils.entity.EntityUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class WebbedSnare extends RightClickAbility {

    public WebbedSnare(SuperSmashLegends plugin, Section config, Kit kit) {
        super(plugin, config, kit);
    }

    private void launch(boolean first) {
        Location location = EntityUtils.center(this.player);
        location.setDirection(this.player.getEyeLocation().getDirection().multiply(-1));

        SnareProjectile projectile = new SnareProjectile(this.plugin, this, this.config.getSection("Projectile"));
        projectile.setOverrideLocation(location);

        if (first) {
            projectile.setSpread(0);
        }

        projectile.launch();
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.player.getWorld().playSound(this.player.getLocation(), Sound.SPIDER_DEATH, 2, 2);
        this.player.setVelocity(this.player.getEyeLocation().getDirection().multiply(this.config.getDouble("Velocity")));

        this.launch(true);

        for (int i = 1; i < this.config.getInt("WebCount"); i++) {
            this.launch(false);
        }
    }

    private static class SnareProjectile extends ItemProjectile {
        private Block webBlock;

        public SnareProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
            super(plugin, ability, config);
        }

        @Override
        public void onTick() {
            if (this.ticksAlive % 3 == 0) {
                new ParticleBuilder(EnumParticle.SNOWBALL).show(this.entity.getLocation());
            }
        }

        private void turnIntoWeb() {
            this.webBlock = this.entity.getLocation().getBlock();
            this.webBlock.setType(Material.WEB);
            int duration = this.config.getInt("WebDuration");
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.webBlock.setType(Material.AIR), duration);
        }

        @Override
        public void onTargetHit(LivingEntity target) {
            this.turnIntoWeb();
            target.setVelocity(new Vector(0, 0, 0));
        }

        @Override
        public void onBlockHit(BlockHitResult result) {
            this.turnIntoWeb();
        }
    }
}
