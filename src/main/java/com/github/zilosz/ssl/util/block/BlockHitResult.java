package com.github.zilosz.ssl.util.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

@Getter
@RequiredArgsConstructor
public class BlockHitResult {
  private final BlockFace face;
  private final Block block;
}
