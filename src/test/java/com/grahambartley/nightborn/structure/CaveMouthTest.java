package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CaveMouthTest {

  private static final Identifier DOOR = Identifier.of("nightborn", "lair_entrance");
  private static final BlockState SILL = Blocks.COBBLED_DEEPSLATE.getDefaultState();
  private static final CaveMouth MOUTH = new CaveMouth(DOOR, 8, 1, 4, -60, 4, 2, SILL);
  private static final BlockPos FLOOR = new BlockPos(0, -30, 0);

  private static final class Rock implements CaveMouth.Ground {

    private final Map<BlockPos, CaveCell> cells = new HashMap<>();
    private final CaveCell rest;

    Rock(final CaveCell rest) {
      this.rest = rest;
    }

    Rock fill(final BlockBox box, final CaveCell cell) {
      BlockPos.stream(box).forEach(pos -> cells.put(pos.toImmutable(), cell));
      return this;
    }

    Rock cave(final int x0, final int floorY, final int z0, final int x1, final int z1) {
      return fill(new BlockBox(x0, floorY + 1, z0, x1, floorY + 3, z1), CaveCell.OPEN);
    }

    @Override
    public CaveCell at(final BlockPos pos) {
      return cells.getOrDefault(pos, rest);
    }
  }

  private static Threshold facing(final Direction out) {
    return new Threshold(FLOOR, out);
  }

  private static BlockBox behind(final Direction out) {
    final BlockPos back = FLOOR.offset(out.getOpposite(), 13);
    return new BlockBox(
        back.getX() - 12, -30, back.getZ() - 12, back.getX() + 12, -19, back.getZ() + 12);
  }

  static Stream<Arguments> caves() {
    return Stream.of(
        Arguments.of(
            "a cave right outside the door needs no move",
            new Rock(CaveCell.SOLID).cave(-3, -30, 1, 3, 6),
            Direction.SOUTH,
            Optional.of(BlockPos.ORIGIN)),
        Arguments.of(
            "a cave three blocks lower drops the lair to meet it",
            new Rock(CaveCell.SOLID).cave(-3, -33, 1, 3, 6),
            Direction.SOUTH,
            Optional.of(new BlockPos(0, -3, 0))),
        Arguments.of(
            "a cave to the east slides the lair over to it",
            new Rock(CaveCell.SOLID).cave(5, -30, -3, 9, 3),
            Direction.EAST,
            Optional.of(new BlockPos(4, 0, 0))),
        Arguments.of(
            "a cave to the north of a north door",
            new Rock(CaveCell.SOLID).cave(-2, -28, -8, 2, -1),
            Direction.NORTH,
            Optional.of(new BlockPos(0, 2, 0))),
        Arguments.of(
            "solid rock all round has no way in",
            new Rock(CaveCell.SOLID),
            Direction.SOUTH,
            Optional.empty()),
        Arguments.of(
            "a cave out of reach is never found",
            new Rock(CaveCell.SOLID).cave(20, -30, 20, 26, 26),
            Direction.SOUTH,
            Optional.empty()),
        Arguments.of(
            "a cave flooded with water is refused",
            new Rock(CaveCell.SOLID)
                .cave(-3, -30, 1, 3, 6)
                .fill(new BlockBox(-3, -29, 1, 3, -29, 6), CaveCell.FLUID),
            Direction.SOUTH,
            Optional.empty()),
        Arguments.of(
            "a lava pool beside the mouth moves the lair clear of it",
            new Rock(CaveCell.SOLID)
                .cave(-3, -30, 1, 3, 6)
                .fill(new BlockBox(1, -30, 1, 1, -30, 2), CaveCell.FLUID),
            Direction.SOUTH,
            Optional.of(new BlockPos(-1, 0, 0))),
        Arguments.of(
            "a one block nook is not a cave",
            new Rock(CaveCell.SOLID).cave(0, -30, 1, 0, 1),
            Direction.SOUTH,
            Optional.empty()),
        Arguments.of(
            "a mouth that opens over a drop is refused",
            new Rock(CaveCell.SOLID)
                .cave(-3, -30, 1, 3, 6)
                .fill(new BlockBox(-3, -36, 2, 3, -30, 6), CaveCell.OPEN),
            Direction.SOUTH,
            Optional.empty()),
        Arguments.of(
            "a mouth that steps one block down into the cave is fine",
            new Rock(CaveCell.SOLID)
                .cave(-3, -30, 1, 3, 6)
                .fill(new BlockBox(-3, -30, 2, 3, -30, 6), CaveCell.OPEN),
            Direction.SOUTH,
            Optional.of(BlockPos.ORIGIN)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("caves")
  void findsTheNearestCaveTheDoorCanOpenOnto(
      final String name,
      final CaveMouth.Ground ground,
      final Direction out,
      final Optional<BlockPos> move) {
    assertEquals(move, MOUTH.meet(facing(out), behind(out), ground));
  }

  @ParameterizedTest(name = "a cave floor at y {0} with the lowest floor at {1} opens: {2}")
  @CsvSource({"-58, -60, true", "-59, -60, true", "-60, -60, false", "-50, -40, false"})
  void neverOpensBelowTheLowestFloor(final int floorY, final int lowest, final boolean opens) {
    final CaveMouth mouth = new CaveMouth(DOOR, 8, 1, 4, lowest, 4, 2, SILL);
    final CaveMouth.Ground ground = new Rock(CaveCell.SOLID).cave(-3, floorY, -3, 3, 6);
    assertEquals(
        opens, mouth.opens(new BlockPos(0, floorY, 0), Direction.SOUTH, LAIR_AWAY, ground));
  }

  @ParameterizedTest(name = "reach {0}, spacing {1}, rise {2} tries {3} moves, nearest first")
  @CsvSource({"0, 1, 0, 1", "2, 1, 1, 75", "4, 2, 2, 125", "24, 2, 12, 15625"})
  void triesEveryMoveNearestFirst(
      final int reach, final int spacing, final int rise, final int count) {
    final List<BlockPos> moves = new CaveMouth(DOOR, reach, spacing, rise, -64, 4, 2, SILL).moves();
    assertEquals(count, moves.size());
    assertEquals(BlockPos.ORIGIN, moves.getFirst());
    for (int i = 1; i < moves.size(); i++) {
      final BlockPos before = moves.get(i - 1);
      final BlockPos after = moves.get(i);
      assertTrue(flat(before) <= flat(after), before + " before " + after);
    }
  }

  private static final BlockBox LAIR_AWAY = new BlockBox(100, -70, 100, 120, -60, 120);

  @ParameterizedTest(name = "{0}: approached by {1} floors, routed {2} steps: {3}")
  @MethodSource("approaches")
  void needsACaveFloorThatWalksToTheDoorAndARouteIntoIt(
      final String name,
      final CaveMouth.Ground ground,
      final int approach,
      final int path,
      final boolean opens) {
    final CaveMouth mouth = new CaveMouth(DOOR, 0, 1, 0, -64, approach, path, SILL);
    assertEquals(opens, mouth.opens(FLOOR.south(), Direction.SOUTH, LAIR_AWAY, ground));
  }

  static Stream<Arguments> approaches() {
    final CaveMouth.Ground hall = new Rock(CaveCell.SOLID).cave(-4, -30, 1, 4, 12);
    return Stream.of(
        Arguments.of("a wide hall", hall, 64, 8, true),
        Arguments.of("a wide hall asked for more floors than it has", hall, 200, 8, false),
        Arguments.of(
            "a narrow pocket", new Rock(CaveCell.SOLID).cave(-1, -30, 1, 1, 4), 64, 2, false),
        Arguments.of(
            "a hall reached only by climbing two blocks",
            new Rock(CaveCell.SOLID)
                .cave(-4, -30, 1, 4, 3)
                .fill(new BlockBox(-4, -33, 4, 4, -30, 14), CaveCell.OPEN),
            30,
            2,
            false),
        Arguments.of(
            "a hall two blocks above the door drops down to it",
            new Rock(CaveCell.SOLID)
                .fill(new BlockBox(-4, -29, 1, 4, -24, 3), CaveCell.OPEN)
                .fill(new BlockBox(-4, -27, 4, 4, -24, 14), CaveCell.OPEN),
            30,
            1,
            true));
  }

  @ParameterizedTest(
      name = "a route of {0} steps runs from the door without leaving the cave floor")
  @CsvSource({"1", "4", "8"})
  void routesStepByStepFromTheDoor(final int path) {
    final CaveMouth mouth = new CaveMouth(DOOR, 0, 1, 0, -64, 1, path, SILL);
    final CaveMouth.Ground hall = new Rock(CaveCell.SOLID).cave(-4, -30, 1, 4, 12);
    final BlockPos door = FLOOR.south();
    final List<BlockPos> route = mouth.route(door, LAIR_AWAY, hall).orElseThrow();
    assertEquals(path + 1, route.size());
    assertEquals(door, route.getFirst());
    for (int i = 1; i < route.size(); i++) {
      assertEquals(1, route.get(i - 1).getManhattanDistance(route.get(i)));
      assertEquals(CaveCell.SOLID, hall.at(route.get(i)));
    }
  }

  @org.junit.jupiter.api.Test
  void neverRoutesThroughTheLairItself() {
    final CaveMouth mouth = new CaveMouth(DOOR, 0, 1, 0, -64, 1, 3, SILL);
    final CaveMouth.Ground corridor = new Rock(CaveCell.SOLID).cave(-6, -30, 1, 6, 1);
    final BlockBox lair = new BlockBox(-6, -30, 1, -1, -20, 3);
    final List<BlockPos> route = mouth.route(FLOOR.south(), lair, corridor).orElseThrow();
    assertTrue(route.stream().noneMatch(lair::contains), route.toString());
  }

  @ParameterizedTest(name = "a floor at y {0} with rise {1} samples y {2} to {3}")
  @CsvSource({"-30, 4, -46, -13", "-10, 12, -34, 15"})
  void samplesOnlyTheDepthTheSearchCanReach(
      final int floorY, final int rise, final int bottom, final int top) {
    final CaveMouth mouth = new CaveMouth(DOOR, 8, 1, rise, -64, 4, 2, SILL);
    assertEquals(
        new DepthBand(bottom, top),
        mouth.band(new Threshold(new BlockPos(0, floorY, 0), Direction.NORTH)));
  }

  private static int flat(final BlockPos move) {
    return move.getX() * move.getX() + move.getZ() * move.getZ();
  }

  @ParameterizedTest(name = "{0} decodes: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"entrance\": \"nightborn:lair_entrance\", \"reach\": 24, \"rise\": 12} | true",
        "{\"entrance\": \"nightborn:lair_entrance\", \"reach\": 49, \"rise\": 12} | false",
        "{\"entrance\": \"nightborn:lair_entrance\", \"reach\": 24, \"rise\": 33} | false",
        "{\"reach\": 24, \"rise\": 12} | false",
        "{\"entrance\": \"nightborn:lair_entrance\", \"reach\": 24, \"rise\": 12, \"path\": 17} | false",
        "{\"entrance\": \"nightborn:lair_entrance\", \"reach\": 24, \"rise\": 12, \"sill\": {\"Name\": \"minecraft:tuff\"}} | true"
      })
  void readsItsSearchFromData(final String json, final boolean decodes) {
    assertEquals(
        decodes, CaveMouth.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
