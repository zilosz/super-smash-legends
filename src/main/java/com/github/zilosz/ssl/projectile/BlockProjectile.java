package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.utils.block.BlockUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

@Setter
public class BlockProjectile extends EmulatedProjectile<FallingBlock> {
    private Material material;
    private byte data;

    public BlockProjectile(Section config, AttackInfo attackInfo) {
        super(config, attackInfo);
        this.material = Material.valueOf(config.getOptionalString("Block.Material").orElse("DIRT"));
        this.data = config.getOptionalByte("Block.Data").orElse((byte) 0);
        this.defaultHitBox = 1;
    }

    @Override
    public FallingBlock createEntity(Location location) {
        return BlockUtils.spawnFallingBlock(location, this.material, this.data);
    }

    @EventHandler
    public void onSolidify(EntityChangeBlockEvent event) {
        if (event.getEntity() == this.entity) {
            event.setCancelled(true);
        }
    }
}
