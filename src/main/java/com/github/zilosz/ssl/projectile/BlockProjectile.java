package com.github.zilosz.ssl.projectile;

import com.github.zilosz.ssl.attack.AttackInfo;
import com.github.zilosz.ssl.util.block.BlockUtils;
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
    material = Material.valueOf(config.getOptionalString("Block.Material").orElse("DIRT"));
    data = config.getByte("Block.Data");
    defaultHitBox = 1;
  }

  @Override
  public FallingBlock createEntity(Location location) {
    return BlockUtils.spawnFallingBlock(location, material, data);
  }

  @EventHandler
  public void onSolidify(EntityChangeBlockEvent event) {
    if (event.getEntity() == entity) {
      event.setCancelled(true);
    }
  }
}
