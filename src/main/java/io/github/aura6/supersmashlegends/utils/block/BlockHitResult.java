package io.github.aura6.supersmashlegends.utils.block;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockHitResult {
    @Getter private final BlockFace face;
    @Getter private final Block block;

    public BlockHitResult(BlockFace face, Block block) {
        this.face = face;
        this.block = block;
    }
}
