package io.github.aura6.supersmashlegends.utils.block;

import io.github.aura6.supersmashlegends.utils.math.MathUtils;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BlockUtils {

    public static BlockFace toSimpleFace(BlockFace face) {
        return face == null ? null : BlockFace.valueOf(face.name().split("_")[0]);
    }

    public static BlockHitResult findBlockHitByBox(Location bottomCenter, double xSize, double ySize, double zSize, double accuracy) {
        Block down = bottomCenter.clone().add(0, -accuracy, 0).getBlock();
        if (down.getType().isSolid()) return new BlockHitResult(BlockFace.UP, down);

        Block up = bottomCenter.clone().add(0, ySize + accuracy, 0).getBlock();
        if (up.getType().isSolid()) return new BlockHitResult(BlockFace.DOWN, up);

        Block west = bottomCenter.clone().add(-xSize / 2 - accuracy, ySize / 2, 0).getBlock();
        if (west.getType().isSolid()) return new BlockHitResult(BlockFace.WEST, west);

        Block east = bottomCenter.clone().add(xSize / 2 + accuracy, ySize / 2, 0).getBlock();
        if (east.getType().isSolid()) return new BlockHitResult(BlockFace.EAST, east);

        Block south = bottomCenter.clone().add(0, ySize / 2, -zSize / 2 - accuracy).getBlock();
        if (south.getType().isSolid()) return new BlockHitResult(BlockFace.SOUTH, south);

        Block north = bottomCenter.clone().add(0, ySize / 2, zSize / 2 + accuracy).getBlock();
        if (north.getType().isSolid()) return new BlockHitResult(BlockFace.NORTH, north);

        return null;
    }

    public static BlockHitResult findBlockHitByEntityBox(Entity entity, Location location, double accuracy) {
        AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox();
        return findBlockHitByBox(location, box.d - box.a, box.e - box.b, box.f - box.c, accuracy);
    }

    public static BlockHitResult findBlockHitByEntityBox(Entity entity, double accuracy) {
        AxisAlignedBB box = ((CraftEntity) entity).getHandle().getBoundingBox();

        if (entity.isOnGround()) {
            return new BlockHitResult(BlockFace.UP, entity.getLocation().subtract(0, 1, 0).getBlock());
        }

        return findBlockHitByBox(entity.getLocation(), box.d - box.a, box.e - box.b, box.f - box.c, accuracy);
    }

    public static BlockHitResult findBlockHitWithRay(Entity entity, Vector direction, int rayRange, double rayStep, double faceAccuracy) {
        Location location = entity.getLocation();

        if (entity.isOnGround()) return new BlockHitResult(BlockFace.UP, location.subtract(0, 1, 0).getBlock());

        BlockRay ray = new BlockRay(location, direction);
        ray.cast(rayRange);
        Block hitBlock = ray.getHitBlock();

        if (hitBlock.isEmpty()) return null;

        BlockFace hitFace = BlockUtils.toSimpleFace(location.getBlock().getFace(hitBlock));

        if (hitFace != null) return new BlockHitResult(hitFace, hitBlock);

        Vector step = direction.clone().normalize().multiply(rayStep);
        double stepped = 0;

        while (stepped <= rayRange) {
            BlockHitResult result = findBlockHitByEntityBox(entity, location, faceAccuracy);

            if (result != null) return result;

            location.add(step);
            stepped += rayStep;
        }

        return new BlockHitResult(null, hitBlock);
    }

    public static void setBlockFast(Location loc, int blockId, byte data) {
        World nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
        Chunk nmsChunk = nmsWorld.getChunkAt(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
        nmsChunk.a(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), ibd);
        loc.getWorld().refreshChunk(nmsChunk.bukkitChunk.getX(), nmsChunk.bukkitChunk.getZ());
    }

    public static void setBlockFast(Location loc, Material material, byte data) {
        setBlockFast(loc, material.getId(), data);
    }

    public static void setBlockFast(Location loc, Material material) {
        setBlockFast(loc, material.getId(), (byte) 2);
    }

    public static boolean isLocationInsideBox(Location location, AxisAlignedBB box) {
        return MathUtils.isBetween(location.getX(), box.a, box.d)
                && MathUtils.isBetween(location.getY(), box.b, box.e)
                && MathUtils.isBetween(location.getZ(), box.c, box.f);
    }
}
