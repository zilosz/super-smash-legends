package com.github.zilosz.ssl.util.block;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

@Getter
public class BlockHitResult {
    private final BlockFace face;
    private final Block block;

    public BlockHitResult(BlockFace face, Block block) {
        this.face = face;
        this.block = block;
    }
}
