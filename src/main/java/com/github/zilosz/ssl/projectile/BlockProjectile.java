package com.github.zilosz.ssl.projectile;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.Ability;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockProjectile extends EmulatedProjectile<FallingBlock> {
    @Setter private Material material;
    @Setter private byte data;

    public BlockProjectile(SSL plugin, Ability ability, Section config) {
        super(plugin, ability, config);

        material = Material.valueOf(config.getOptionalString("Block.Material").orElse("OBSIDIAN"));
        data = config.getOptionalByte("Block.Data").orElse((byte) 0);
    }

    @Override
    public double defaultHitBox() {
        return 1;
    }

    @Override
    public FallingBlock createEntity(Location location) {
        FallingBlock block = location.getWorld().spawnFallingBlock(location, material, data);
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
