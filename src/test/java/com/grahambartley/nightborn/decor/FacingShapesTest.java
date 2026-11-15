package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FacingShapesTest {

  private static final Vec3d BACK_LEFT = new Vec3d(0.25, 0.5, 0.875);

  static Stream<Arguments> turns() {
    return Stream.of(
        Arguments.of(Direction.NORTH, new Vec3d(0.25, 0.5, 0.875)),
        Arguments.of(Direction.SOUTH, new Vec3d(0.75, 0.5, 0.125)),
        Arguments.of(Direction.WEST, new Vec3d(0.875, 0.5, 0.75)),
        Arguments.of(Direction.EAST, new Vec3d(0.125, 0.5, 0.25)));
  }

  @ParameterizedTest(name = "facing {0} puts the back left point at {1}")
  @MethodSource("turns")
  void turnsPointsAboutTheBlockCentre(final Direction facing, final Vec3d expected) {
    final Vec3d turned = FacingShapes.turn(BACK_LEFT, facing);
    assertEquals(expected.x, turned.x, 1.0e-9);
    assertEquals(expected.y, turned.y, 1.0e-9);
    assertEquals(expected.z, turned.z, 1.0e-9);
  }

  static Stream<Arguments> boxes() {
    return Stream.of(
        Arguments.of(Direction.NORTH, new Box(0.0, 0.0, 0.8125, 1.0, 1.0, 1.0)),
        Arguments.of(Direction.SOUTH, new Box(0.0, 0.0, 0.0, 1.0, 1.0, 0.1875)),
        Arguments.of(Direction.WEST, new Box(0.8125, 0.0, 0.0, 1.0, 1.0, 1.0)),
        Arguments.of(Direction.EAST, new Box(0.0, 0.0, 0.0, 0.1875, 1.0, 1.0)));
  }

  @ParameterizedTest(name = "a back panel facing {0} sits at {1}")
  @MethodSource("boxes")
  void turnsShapesToEveryHorizontalFacing(final Direction facing, final Box expected) {
    final Box box =
        FacingShapes.fromNorth(Block.createCuboidShape(0, 0, 13, 16, 16, 16))
            .get(facing)
            .getBoundingBox();
    assertEquals(expected, box);
  }

  @ParameterizedTest(name = "facing {0}")
  @MethodSource("turns")
  void placesAWickInTheWorldAtTheTurnedPointOfItsBlock(final Direction facing, final Vec3d turned) {
    final Vec3d at = FacingShapes.atWick(new BlockPos(10, 64, -3), BACK_LEFT, facing);
    assertEquals(10 + turned.x, at.x, 1.0e-9);
    assertEquals(64 + turned.y, at.y, 1.0e-9);
    assertEquals(-3 + turned.z, at.z, 1.0e-9);
  }
}
