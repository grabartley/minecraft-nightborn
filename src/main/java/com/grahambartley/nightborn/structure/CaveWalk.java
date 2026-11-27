package com.grahambartley.nightborn.structure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class CaveWalk {

  public static final int REACH = 12;
  public static final int AIRSPACE = 3;
  public static final int SAFE_DROP = 3;

  private CaveWalk() {}

  public static boolean stands(final BlockPos floor, final CaveMouth.Ground ground) {
    return ground.at(floor) == CaveCell.SOLID && clear(floor, ground);
  }

  public static boolean clear(final BlockPos floor, final CaveMouth.Ground ground) {
    return ground.at(floor.up()) == CaveCell.OPEN && ground.at(floor.up(2)) == CaveCell.OPEN;
  }

  public static boolean walks(
      final BlockPos from, final BlockPos to, final CaveMouth.Ground ground) {
    if (to.getY() > from.getY()) {
      return to.getY() - from.getY() == 1 && ground.at(from.up(AIRSPACE)) == CaveCell.OPEN;
    }
    if (from.getY() - to.getY() > SAFE_DROP) {
      return false;
    }
    for (int y = to.getY() + 1; y <= from.getY() + 2; y++) {
      if (ground.at(new BlockPos(to.getX(), y, to.getZ())) != CaveCell.OPEN) {
        return false;
      }
    }
    return true;
  }

  public static boolean approached(
      final BlockPos door, final BlockBox lair, final CaveMouth.Ground ground, final int floors) {
    final Set<BlockPos> seen = new HashSet<>();
    final ArrayDeque<BlockPos> open = new ArrayDeque<>();
    seen.add(door);
    open.add(door);
    int counted = 0;
    while (!open.isEmpty()) {
      final BlockPos to = open.poll();
      if (++counted >= floors) {
        return true;
      }
      for (final Direction side : Direction.Type.HORIZONTAL) {
        for (int rise = -1; rise <= SAFE_DROP; rise++) {
          final BlockPos from = to.offset(side).up(rise);
          if (near(from, door)
              && !seen.contains(from)
              && outside(from, lair)
              && stands(from, ground)
              && walks(from, to, ground)) {
            seen.add(from);
            open.add(from);
          }
        }
      }
    }
    return false;
  }

  public static Optional<List<BlockPos>> route(
      final BlockPos door, final BlockBox lair, final CaveMouth.Ground ground, final int steps) {
    final Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
    final Map<BlockPos, Integer> taken = new HashMap<>();
    final ArrayDeque<BlockPos> open = new ArrayDeque<>();
    taken.put(door, 0);
    open.add(door);
    while (!open.isEmpty()) {
      final BlockPos at = open.poll();
      final int far = taken.get(at);
      if (far == steps) {
        final List<BlockPos> route = new ArrayList<>();
        for (BlockPos back = at; back != null; back = cameFrom.get(back)) {
          route.add(back);
        }
        return Optional.of(List.copyOf(route.reversed()));
      }
      for (final Direction side : Direction.Type.HORIZONTAL) {
        for (int rise = -1; rise <= 1; rise++) {
          final BlockPos next = at.offset(side).up(rise);
          if (near(next, door)
              && !taken.containsKey(next)
              && outside(next, lair)
              && stands(next, ground)
              && walks(at, next, ground)
              && walks(next, at, ground)) {
            taken.put(next, far + 1);
            cameFrom.put(next, at);
            open.add(next);
          }
        }
      }
    }
    return Optional.empty();
  }

  static boolean outside(final BlockPos floor, final BlockBox lair) {
    return !lair.contains(floor) && !lair.contains(floor.up()) && !lair.contains(floor.up(2));
  }

  static boolean near(final BlockPos pos, final BlockPos start) {
    return Math.abs(pos.getX() - start.getX()) <= REACH
        && Math.abs(pos.getY() - start.getY()) <= REACH
        && Math.abs(pos.getZ() - start.getZ()) <= REACH;
  }
}
