package com.grahambartley.nightborn.coffin;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CollisionView;

public final class CoffinStepOut {

  private CoffinStepOut() {}

  public static Optional<Vec3d> spot(
      final EntityType<?> type, final CollisionView world, final BlockPos head) {
    final BlockState state = world.getBlockState(head);
    if (!CoffinBlock.isStanding(state)) {
      return Optional.empty();
    }
    final Direction facing = state.get(BedBlock.FACING);
    final List<BlockPos> cells = CoffinLayout.stepOutCells(head, facing);
    for (final boolean avoidHazards : new boolean[] {true, false}) {
      for (final BlockPos cell : cells) {
        final Vec3d spot = Dismounting.findRespawnPos(type, world, cell, avoidHazards);
        if (spot != null) {
          return Optional.of(spot);
        }
      }
    }
    return Optional.empty();
  }
}
