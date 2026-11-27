package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record CaveMouth(
    Identifier entrance,
    int reach,
    int spacing,
    int rise,
    int lowest,
    int approach,
    int path,
    BlockState sill) {

  public static final int FARTHEST_REACH = 48;
  public static final int WIDEST_SPACING = 8;
  public static final int STEEPEST_RISE = 32;
  public static final int WIDEST_APPROACH = 1024;
  public static final int LONGEST_PATH = 16;
  static final int SAFE_DROP = 3;
  static final int AIRSPACE = 3;
  static final int DRY_REACH = 3;
  static final int ROOM_REACH = 12;

  public static final Codec<CaveMouth> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("entrance").forGetter(CaveMouth::entrance),
                      Codec.intRange(0, FARTHEST_REACH)
                          .fieldOf("reach")
                          .forGetter(CaveMouth::reach),
                      Codec.intRange(1, WIDEST_SPACING)
                          .optionalFieldOf("spacing", 2)
                          .forGetter(CaveMouth::spacing),
                      Codec.intRange(0, STEEPEST_RISE).fieldOf("rise").forGetter(CaveMouth::rise),
                      Codec.intRange(Summit.LOWEST, Summit.HIGHEST)
                          .optionalFieldOf("lowest", Summit.LOWEST)
                          .forGetter(CaveMouth::lowest),
                      Codec.intRange(1, WIDEST_APPROACH)
                          .optionalFieldOf("approach", 64)
                          .forGetter(CaveMouth::approach),
                      Codec.intRange(1, LONGEST_PATH)
                          .optionalFieldOf("path", 8)
                          .forGetter(CaveMouth::path),
                      BlockState.CODEC
                          .optionalFieldOf("sill", Blocks.COBBLED_DEEPSLATE.getDefaultState())
                          .forGetter(CaveMouth::sill))
                  .apply(instance, CaveMouth::new));

  @FunctionalInterface
  public interface Ground {
    CaveCell at(BlockPos pos);
  }

  public List<BlockPos> moves() {
    final List<BlockPos> moves = new ArrayList<>();
    for (int dx = -reach; dx <= reach; dx += spacing) {
      for (int dz = -reach; dz <= reach; dz += spacing) {
        for (int dy = -rise; dy <= rise; dy++) {
          moves.add(new BlockPos(dx, dy, dz));
        }
      }
    }
    moves.sort(
        Comparator.comparingInt(
                (BlockPos move) -> move.getX() * move.getX() + move.getZ() * move.getZ())
            .thenComparingInt(move -> Math.abs(move.getY()))
            .thenComparingInt(BlockPos::getY)
            .thenComparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ));
    return moves;
  }

  public DepthBand band(final Threshold way) {
    final int floor = way.floor().getY();
    return new DepthBand(floor - rise - ROOM_REACH, floor + rise + ROOM_REACH + 1);
  }

  public Optional<BlockPos> meet(final Threshold way, final BlockBox lair, final Ground ground) {
    final BlockPos outside = way.floor().offset(way.out());
    return moves().stream()
        .filter(
            move ->
                opens(
                    outside.add(move),
                    way.out(),
                    lair.offset(move.getX(), move.getY(), move.getZ()),
                    ground))
        .findFirst();
  }

  public boolean opens(
      final BlockPos floor, final Direction out, final BlockBox lair, final Ground ground) {
    if (floor.getY() - 1 < lowest || ground.at(floor) != CaveCell.SOLID) {
      return false;
    }
    final BlockPos beyond = floor.offset(out);
    final BlockPos farther = beyond.offset(out);
    return clear(floor, ground)
        && clear(beyond, ground)
        && ground.at(farther.up()) == CaveCell.OPEN
        && (ground.at(beyond) == CaveCell.SOLID || ground.at(beyond.down()) == CaveCell.SOLID)
        && dry(floor, ground)
        && dry(beyond, ground)
        && approached(floor, lair, ground)
        && route(floor, lair, ground).isPresent();
  }

  public Optional<List<BlockPos>> route(
      final BlockPos door, final BlockBox lair, final Ground ground) {
    final Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
    final Map<BlockPos, Integer> steps = new HashMap<>();
    final ArrayDeque<BlockPos> open = new ArrayDeque<>();
    steps.put(door, 0);
    open.add(door);
    while (!open.isEmpty()) {
      final BlockPos at = open.poll();
      final int taken = steps.get(at);
      if (taken == path) {
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
              && !steps.containsKey(next)
              && outside(next, lair)
              && stands(next, ground)
              && walks(at, next, ground)
              && walks(next, at, ground)) {
            steps.put(next, taken + 1);
            cameFrom.put(next, at);
            open.add(next);
          }
        }
      }
    }
    return Optional.empty();
  }

  public boolean approached(final BlockPos door, final BlockBox lair, final Ground ground) {
    final Set<BlockPos> seen = new HashSet<>();
    final ArrayDeque<BlockPos> open = new ArrayDeque<>();
    seen.add(door);
    open.add(door);
    int floors = 0;
    while (!open.isEmpty()) {
      final BlockPos to = open.poll();
      if (++floors >= approach) {
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

  private static boolean stands(final BlockPos floor, final Ground ground) {
    return ground.at(floor) == CaveCell.SOLID && clear(floor, ground);
  }

  private static boolean walks(final BlockPos from, final BlockPos to, final Ground ground) {
    if (to.getY() > from.getY()) {
      return ground.at(from.up(AIRSPACE)) == CaveCell.OPEN;
    }
    for (int y = to.getY() + 1; y <= from.getY() + 2; y++) {
      if (ground.at(new BlockPos(to.getX(), y, to.getZ())) != CaveCell.OPEN) {
        return false;
      }
    }
    return true;
  }

  private static boolean outside(final BlockPos floor, final BlockBox lair) {
    return !lair.contains(floor) && !lair.contains(floor.up()) && !lair.contains(floor.up(2));
  }

  private static boolean near(final BlockPos pos, final BlockPos start) {
    return Math.abs(pos.getX() - start.getX()) <= ROOM_REACH
        && Math.abs(pos.getY() - start.getY()) <= ROOM_REACH
        && Math.abs(pos.getZ() - start.getZ()) <= ROOM_REACH;
  }

  private static boolean clear(final BlockPos floor, final Ground ground) {
    return ground.at(floor.up()) == CaveCell.OPEN && ground.at(floor.up(2)) == CaveCell.OPEN;
  }

  private static boolean dry(final BlockPos floor, final Ground ground) {
    for (final BlockPos cell : BlockPos.iterate(floor.add(-1, -1, -1), floor.add(1, AIRSPACE, 1))) {
      if (ground.at(cell) == CaveCell.FLUID) {
        return false;
      }
    }
    return true;
  }
}
