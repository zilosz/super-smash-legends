package io.github.aura6.supersmashlegends.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.github.aura6.supersmashlegends.SuperSmashLegends;
import io.github.aura6.supersmashlegends.attribute.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockProjectile extends EmulatedProjectile<FallingBlock> {

    public BlockProjectile(SuperSmashLegends plugin, Ability ability, Section config) {
        super(plugin, ability, config);
    }

    @Override
    public double defaultHitBox() {
        return 1;
    }

    @Override
    public FallingBlock createEntity(Location location) {
        Material type = Material.valueOf(config.getString("Block.Material"));
        byte data = config.getOptionalByte("Block.Data").orElse((byte) 0);

        FallingBlock block = location.getWorld().spawnFallingBlock(location, type, data);
        block.setDropItem(false);
        block.setHurtEntities(false);

        return block;
    }

    @EventHandler
    public void onSolidify(EntityChangeBlockEvent event) {
        if (event.getEntity() == entity) {
            event.setCancelled(true);
        }
    }
}
