package com.github.zilosz.ssl.util.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class MaterialInfo {
  private final Material material;
  private final byte data;

  public static MaterialInfo fromBlock(Block block) {
    return new MaterialInfo(block.getType(), block.getData());
  }

  public void apply(Block block) {
    block.setType(material);
    block.setData(data);
  }
}
