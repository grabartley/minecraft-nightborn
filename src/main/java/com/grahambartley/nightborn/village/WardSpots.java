package com.grahambartley.nightborn.village;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class WardSpots {

  static final int BELL_REACH = 2;

  public record Spot(BlockPos pos, Direction facing) {}

  private WardSpots() {}

  public static List<Spot> besideDoor(final BlockPos lowerDoor, final Direction doorFacing) {
    final List<Spot> spots = new ArrayList<>();
    final BlockPos upper = lowerDoor.up();
    for (final Direction side : List.of(doorFacing, doorFacing.getOpposite())) {
      for (final Direction along :
          List.of(doorFacing.rotateYClockwise(), doorFacing.rotateYCounterclockwise())) {
        spots.add(new Spot(upper.offset(along).offset(side), side));
      }
    }
    return spots;
  }

  public static List<Spot> aroundBell(final BlockPos bell) {
    final List<Spot> spots = new ArrayList<>();
    for (int dy = 0; dy >= -1; dy--) {
      for (int dx = -BELL_REACH; dx <= BELL_REACH; dx++) {
        for (int dz = -BELL_REACH; dz <= BELL_REACH; dz++) {
          if (dx == 0 && dz == 0) {
            continue;
          }
          for (final Direction facing : Direction.Type.HORIZONTAL) {
            spots.add(new Spot(bell.add(dx, dy, dz), facing));
          }
        }
      }
    }
    spots.sort(Comparator.comparingDouble(spot -> spot.pos().getSquaredDistance(bell)));
    return spots;
  }
}
