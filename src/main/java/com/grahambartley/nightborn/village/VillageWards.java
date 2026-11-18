package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.gear.GarlicStringBlock;
import com.grahambartley.nightborn.gear.GearBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class VillageWards {

  private VillageWards() {}

  public static boolean hangs(
      final VillageStep step, final boolean hung, final boolean hungTonight) {
    return step.atLeast(VillageStep.WARDED) && !hung && !hungTonight;
  }

  public static boolean takesDown(final VillageStep step, final boolean hung) {
    return !step.atLeast(VillageStep.WARDED) && hung;
  }

  static VillageRecord keep(
      final ServerWorld world,
      final BlockPos bell,
      final VillageStep step,
      final VillageRecord record,
      final List<VillagerEntity> villagers) {
    final List<BlockPos> standing =
        record.wards().stream().filter(pos -> isWard(world.getBlockState(pos))).toList();
    final long night = VillageSuspicion.night(world);
    if (hangs(step, !standing.isEmpty(), record.wardNight() == night)) {
      return record.hung(night, hang(world, bell, villagers));
    }
    if (takesDown(step, !standing.isEmpty())) {
      standing.forEach(pos -> world.removeBlock(pos, false));
      return record.withWards(List.of());
    }
    return record.withWards(standing);
  }

  static VillageRecord takeDown(final ServerWorld world, final VillageRecord record) {
    record.wards().stream()
        .filter(pos -> isWard(world.getBlockState(pos)))
        .forEach(pos -> world.removeBlock(pos, false));
    return record.withWards(List.of());
  }

  private static List<BlockPos> hang(
      final ServerWorld world, final BlockPos bell, final List<VillagerEntity> villagers) {
    final List<BlockPos> placed = new ArrayList<>();
    for (final BlockPos door : VillageDoors.doors(world, villagers)) {
      final BlockState state = world.getBlockState(door);
      if (state.getBlock() instanceof DoorBlock) {
        place(
                world,
                WardSpots.besideDoor(door, state.get(DoorBlock.FACING)),
                GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, false))
            .ifPresent(placed::add);
      }
    }
    place(world, WardSpots.aroundBell(bell), GearBlocks.GOLD_WALL_CRUCIFIX.getDefaultState())
        .ifPresent(placed::add);
    return placed;
  }

  private static Optional<BlockPos> place(
      final ServerWorld world, final List<WardSpots.Spot> spots, final BlockState ward) {
    for (final WardSpots.Spot spot : spots) {
      final BlockState state = ward.with(HorizontalFacingBlock.FACING, spot.facing());
      if (world.isAir(spot.pos()) && state.canPlaceAt(world, spot.pos())) {
        world.setBlockState(spot.pos(), state, Block.NOTIFY_ALL);
        return Optional.of(spot.pos());
      }
    }
    return Optional.empty();
  }

  private static boolean isWard(final BlockState state) {
    return state.isOf(GearBlocks.GARLIC_STRING) || state.isOf(GearBlocks.GOLD_WALL_CRUCIFIX);
  }
}
