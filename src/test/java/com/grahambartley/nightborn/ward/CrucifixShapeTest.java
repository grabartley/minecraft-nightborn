package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class CrucifixShapeTest {

  private static final BlockPos CENTRE = new BlockPos(10, 64, -3);

  private static Set<BlockPos> cross(final Direction arms, final int below) {
    final Set<BlockPos> blocks = new HashSet<>();
    blocks.add(CENTRE.up());
    blocks.add(CENTRE);
    for (int i = 1; i <= below; i++) {
      blocks.add(CENTRE.down(i));
    }
    blocks.add(CENTRE.offset(arms));
    blocks.add(CENTRE.offset(arms.getOpposite()));
    return blocks;
  }

  @ParameterizedTest(name = "arms {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("a cross is found facing any horizontal direction")
  void anyFacing(final Direction arms) {
    assertTrue(CrucifixShape.centredAt(CENTRE, cross(arms, 1)::contains));
  }

  static Stream<Arguments> shapes() {
    final Set<BlockPos> tall = cross(Direction.EAST, 4);
    final Set<BlockPos> noTop = cross(Direction.EAST, 1);
    noTop.remove(CENTRE.up());
    final Set<BlockPos> noFoot = cross(Direction.EAST, 0);
    final Set<BlockPos> oneArm = cross(Direction.EAST, 1);
    oneArm.remove(CENTRE.west());
    final Set<BlockPos> longArm = cross(Direction.EAST, 1);
    longArm.add(CENTRE.east(2));
    final Set<BlockPos> tallTop = cross(Direction.EAST, 1);
    tallTop.add(CENTRE.up(2));
    final Set<BlockPos> thickArm = cross(Direction.EAST, 1);
    thickArm.add(CENTRE.east().down());
    final Set<BlockPos> plus = cross(Direction.EAST, 1);
    plus.add(CENTRE.north());
    plus.add(CENTRE.south());
    final Set<BlockPos> wall = new HashSet<>();
    BlockPos.iterate(CENTRE.add(-1, -1, 0), CENTRE.add(1, 1, 0))
        .forEach(pos -> wall.add(pos.toImmutable()));
    return Stream.of(
        Arguments.of("a long column", tall, true),
        Arguments.of("arms on both axes", plus, true),
        Arguments.of("no top above the arms", noTop, false),
        Arguments.of("a column of two", noFoot, false),
        Arguments.of("one arm", oneArm, false),
        Arguments.of("an arm two blocks long", longArm, false),
        Arguments.of("arms two below the top", tallTop, false),
        Arguments.of("an arm two blocks thick", thickArm, false),
        Arguments.of("a solid wall", wall, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shapes")
  @DisplayName("only a column of three or more with single arms one below the top counts")
  void rules(final String name, final Set<BlockPos> blocks, final boolean expected) {
    assertEquals(expected, CrucifixShape.centredAt(CENTRE, blocks::contains));
  }

  static Stream<BlockPos> changedBlocks() {
    return Stream.of(
        CENTRE,
        CENTRE.up(),
        CENTRE.up(2),
        CENTRE.down(),
        CENTRE.east(),
        CENTRE.east(2),
        CENTRE.west(2),
        CENTRE.north(),
        CENTRE.south(2),
        CENTRE.east().up(),
        CENTRE.west().down());
  }

  @ParameterizedTest(name = "changed {0}")
  @MethodSource("changedBlocks")
  @DisplayName("a change to any block a shape reads re-checks that shape's centre")
  void changeReachesTheCentre(final BlockPos changed) {
    final List<BlockPos> centres = CrucifixShape.centresTouching(changed);
    assertTrue(centres.contains(CENTRE));
    assertEquals(centres.size(), new HashSet<>(centres).size());
  }

  static Stream<Arguments> parts() {
    return Stream.of(
        Arguments.of(CENTRE, true),
        Arguments.of(CENTRE.up(), true),
        Arguments.of(CENTRE.down(), true),
        Arguments.of(CENTRE.east(), true),
        Arguments.of(CENTRE.north(), true),
        Arguments.of(CENTRE.east().up(), false),
        Arguments.of(CENTRE.up(2), false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("parts")
  @DisplayName("a sight line that lands on the cross counts as seeing it")
  void partOf(final BlockPos block, final boolean expected) {
    assertEquals(expected, CrucifixShape.partOf(CENTRE, block));
  }
}
