package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
  static final int DRY_REACH = 3;

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

  private static final Map<List<Integer>, List<BlockPos>> MOVES = new ConcurrentHashMap<>();

  public record Opening(BlockPos move, List<BlockPos> route) {}

  public List<BlockPos> moves() {
    return MOVES.computeIfAbsent(List.of(reach, spacing, rise), key -> sortedMoves());
  }

  private List<BlockPos> sortedMoves() {
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
    return List.copyOf(moves);
  }

  public DepthBand band(final Threshold way) {
    final int floor = way.floor().getY();
    return new DepthBand(
        floor - rise - CaveWalk.REACH - 1, floor + rise + CaveWalk.REACH + CaveWalk.AIRSPACE + 1);
  }

  public Optional<Opening> meet(final Threshold way, final BlockBox lair, final Ground ground) {
    final BlockPos outside = way.step();
    for (final BlockPos move : moves()) {
      final Optional<List<BlockPos>> route =
          opening(
              outside.add(move),
              way.out(),
              lair.offset(move.getX(), move.getY(), move.getZ()),
              ground);
      if (route.isPresent()) {
        return Optional.of(new Opening(move, route.get()));
      }
    }
    return Optional.empty();
  }

  public Optional<List<BlockPos>> opening(
      final BlockPos floor, final Direction out, final BlockBox lair, final Ground ground) {
    if (floor.getY() - 1 < lowest || !CaveWalk.stands(floor, ground)) {
      return Optional.empty();
    }
    final BlockPos beyond = floor.offset(out);
    final BlockPos farther = beyond.offset(out);
    final boolean mouth =
        CaveWalk.clear(beyond, ground)
            && ground.at(farther.up()) == CaveCell.OPEN
            && (ground.at(beyond) == CaveCell.SOLID || ground.at(beyond.down()) == CaveCell.SOLID)
            && dry(floor, ground)
            && dry(beyond, ground)
            && CaveWalk.approached(floor, lair, ground, approach);
    return mouth ? CaveWalk.route(floor, lair, ground, path) : Optional.empty();
  }

  private static boolean dry(final BlockPos floor, final Ground ground) {
    for (final BlockPos cell :
        BlockPos.iterate(
            floor.add(-DRY_REACH, -CaveWalk.SAFE_DROP - 1, -DRY_REACH),
            floor.add(DRY_REACH, CaveWalk.AIRSPACE, DRY_REACH))) {
      if (ground.at(cell) == CaveCell.FLUID) {
        return false;
      }
    }
    return true;
  }
}
