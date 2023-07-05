package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.attribute.Ability;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockProjectile extends EmulatedProjectile<FallingBlock> {
    @Setter private Material material;
    @Setter private byte data;

    public BlockProjectile(Ability ability, Section config) {
        super(ability, config);
        this.material = Material.valueOf(config.getOptionalString("Block.Material").orElse("OBSIDIAN"));
        this.data = config.getOptionalByte("Block.Data").orElse((byte) 0);
        this.defaultHitBox = 1;
    }

    @Override
    public FallingBlock createEntity(Location location) {
        FallingBlock block = location.getWorld().spawnFallingBlock(location, this.material, this.data);
        block.setDropItem(false);
        block.setHurtEntities(false);
        return block;
    }

    @EventHandler
    public void onSolidify(EntityChangeBlockEvent event) {
        if (event.getEntity() == this.entity) {
            event.setCancelled(true);
        }
    }
}
