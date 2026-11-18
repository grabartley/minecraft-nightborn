package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.Nightborn;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class Harvest {

  public static final TagKey<Block> CROPS = tag("gather_crops");
  public static final TagKey<Block> WOOD = tag("gather_wood");
  public static final TagKey<Block> ORES = tag("gather_ores");

  private Harvest() {}

  private static TagKey<Block> tag(final String path) {
    return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, path));
  }

  public static GatherTarget kind(final ServerWorld world, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    final boolean wood = state.isIn(WOOD);
    return GatherTarget.of(
        state.isIn(CROPS),
        state.getBlock() instanceof CropBlock crop && crop.isMature(state),
        wood,
        wood && inATree(world, pos),
        state.isIn(ORES));
  }

  public static boolean reap(
      final ServerWorld world,
      final BlockPos pos,
      final MobEntity worker,
      final SimpleInventory carried) {
    final GatherTarget kind = kind(world, pos);
    if (kind == GatherTarget.NONE) {
      return false;
    }
    final List<BlockPos> felled = kind == GatherTarget.WOOD ? trunk(world, pos) : List.of(pos);
    final BlockState first = world.getBlockState(pos);
    final List<ItemStack> drops = new ArrayList<>();
    for (final BlockPos block : felled) {
      drops.addAll(
          Block.getDroppedStacks(
              world.getBlockState(block),
              world,
              block,
              world.getBlockEntity(block),
              worker,
              ItemStack.EMPTY));
    }
    if (!CarryRoom.fits(carried, drops)) {
      return false;
    }
    for (final BlockPos block : felled) {
      world.breakBlock(block, false, worker);
    }
    drops.forEach(carried::addStack);
    if (kind.replants()) {
      replant(world, pos, first, carried);
    }
    return true;
  }

  private static void replant(
      final ServerWorld world,
      final BlockPos pos,
      final BlockState harvested,
      final SimpleInventory carried) {
    final Item seed = harvested.getBlock().getPickStack(world, pos, harvested).getItem();
    final BlockState sown = harvested.getBlock().getDefaultState();
    if (carried.count(seed) > 0 && sown.canPlaceAt(world, pos)) {
      carried.removeItem(seed, 1);
      world.setBlockState(pos, sown);
    }
  }

  static List<BlockPos> trunk(final ServerWorld world, final BlockPos base) {
    final List<BlockPos> trunk = new ArrayList<>();
    BlockPos.Mutable at = base.mutableCopy();
    while (world.getBlockState(at).isIn(WOOD) && !world.isOutOfHeightLimit(at)) {
      trunk.add(at.toImmutable());
      at = at.move(Direction.UP);
    }
    return trunk;
  }

  static boolean inATree(final ServerWorld world, final BlockPos base) {
    for (final BlockPos log : trunk(world, base)) {
      for (final Direction side : Direction.values()) {
        final BlockState next = world.getBlockState(log.offset(side));
        if (next.getBlock() instanceof LeavesBlock && !next.get(LeavesBlock.PERSISTENT)) {
          return true;
        }
      }
    }
    return false;
  }
}
