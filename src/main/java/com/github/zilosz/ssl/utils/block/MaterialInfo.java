package com.github.zilosz.ssl.utils.block;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

@Getter
public class MaterialInfo {
    private final Material material;
    private final byte data;

    public MaterialInfo(Material material, byte data) {
        this.material = material;
        this.data = data;
    }

    public static MaterialInfo fromBlock(Block block) {
        return new MaterialInfo(block.getType(), block.getData());
    }

    public void apply(Block block) {
        block.setType(this.material);
        block.setData(this.data);
    }
}
