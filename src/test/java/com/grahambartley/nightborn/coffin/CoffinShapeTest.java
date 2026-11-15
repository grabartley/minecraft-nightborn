package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinShapeTest {

  private static final double PIXEL = 1.0 / 16.0;

  static Stream<Arguments> cells() {
    return Stream.of(BedPart.values())
        .flatMap(
            part -> Direction.Type.HORIZONTAL.stream().map(facing -> Arguments.of(part, facing)));
  }

  @ParameterizedTest(name = "{0} facing {1}")
  @MethodSource("cells")
  @DisplayName("every cell stays inside its block and stands as tall as the closed lid")
  void staysInsideItsCell(final BedPart part, final Direction facing) {
    final Box box = CoffinShape.of(part, facing, false).getBoundingBox();
    assertTrue(box.minX >= 0 && box.minZ >= 0 && box.maxX <= 1 && box.maxZ <= 1);
    assertEquals(0.0, box.minY, 1.0E-9);
    assertEquals(CoffinShape.HEIGHT * PIXEL, box.maxY, 1.0E-9);
  }

  static Stream<Arguments> widths() {
    return Stream.of(
        Arguments.of(BedPart.HEAD, Direction.NORTH, 0, 16),
        Arguments.of(BedPart.FOOT, Direction.NORTH, 0, 16),
        Arguments.of(BedPart.HEAD, Direction.EAST, 0, 16),
        Arguments.of(BedPart.FOOT, Direction.WEST, 0, 16));
  }

  @ParameterizedTest(name = "{0} facing {1} spans {2}..{3}")
  @MethodSource("widths")
  @DisplayName("the shoulders and hips fill the block across both cells")
  void widensAtTheShoulders(
      final BedPart part, final Direction facing, final int from, final int to) {
    final Box box = CoffinShape.of(part, facing, false).getBoundingBox();
    final boolean alongX = facing.getAxis() == Direction.Axis.Z;
    assertEquals(from * PIXEL, alongX ? box.minX : box.minZ, 1.0E-9);
    assertEquals(to * PIXEL, alongX ? box.maxX : box.maxZ, 1.0E-9);
  }

  static Stream<Arguments> ends() {
    return Stream.of(
        Arguments.of(BedPart.HEAD, 6.0 / 16.0, 0.5 / 16.0, true),
        Arguments.of(BedPart.HEAD, 0.25 / 16.0, 0.5 / 16.0, false),
        Arguments.of(BedPart.FOOT, 8.0 / 16.0, 15.5 / 16.0, true),
        Arguments.of(BedPart.FOOT, 2.5 / 16.0, 15.5 / 16.0, false));
  }

  @ParameterizedTest(name = "{0} at x {1}, z {2}: solid {3}")
  @MethodSource("ends")
  @DisplayName("the head and foot ends are narrower than the shoulders")
  void tapersAtTheEnds(final BedPart part, final double x, final double z, final boolean solid) {
    final VoxelShape shape = CoffinShape.of(part, Direction.NORTH, false);
    final boolean inside =
        shape.getBoundingBoxes().stream()
            .anyMatch(box -> x > box.minX && x < box.maxX && z > box.minZ && z < box.maxZ);
    assertEquals(solid, inside);
    assertFalse(shape.isEmpty());
  }

  @ParameterizedTest(name = "{0} facing {1}")
  @MethodSource("cells")
  @DisplayName(
      "a standing cell fills its block's height and is as deep as the shut lid, back to the wall")
  void standsAgainstTheWall(final BedPart part, final Direction facing) {
    final Box box = CoffinShape.of(part, facing, true).getBoundingBox();
    assertEquals(0.0, box.minY, 1.0E-9);
    assertEquals(1.0, box.maxY, 1.0E-9);
    final double back = facing.getAxis() == Direction.Axis.Z ? box.minZ : box.minX;
    final double front = facing.getAxis() == Direction.Axis.Z ? box.maxZ : box.maxX;
    final double depth = front - back;
    assertEquals(CoffinShape.HEIGHT * PIXEL, depth, 1.0E-9);
    final double wallSide =
        facing.getDirection() == Direction.AxisDirection.NEGATIVE ? back : 1.0 - front;
    assertEquals(0.0, wallSide, 1.0E-9);
  }

  static Stream<Arguments> uprightPoints() {
    return Stream.of(
        Arguments.of(BedPart.HEAD, 8.0 / 16.0, 15.5 / 16.0, true),
        Arguments.of(BedPart.HEAD, 0.25 / 16.0, 15.5 / 16.0, false),
        Arguments.of(BedPart.HEAD, 1.5 / 16.0, 6.0 / 16.0, true),
        Arguments.of(BedPart.FOOT, 2.5 / 16.0, 0.5 / 16.0, false),
        Arguments.of(BedPart.FOOT, 8.0 / 16.0, 0.5 / 16.0, true));
  }

  @ParameterizedTest(name = "{0} at x {1}, y {2}: solid {3}")
  @MethodSource("uprightPoints")
  @DisplayName("a standing coffin keeps its shoulders high and its narrow ends at top and bottom")
  void keepsItsOutlineUpright(
      final BedPart part, final double x, final double y, final boolean solid) {
    final VoxelShape shape = CoffinShape.of(part, Direction.NORTH, true);
    final double z = 0.25;
    final boolean inside =
        shape.getBoundingBoxes().stream()
            .anyMatch(
                box ->
                    x > box.minX
                        && x < box.maxX
                        && y > box.minY
                        && y < box.maxY
                        && z > box.minZ
                        && z < box.maxZ);
    assertEquals(solid, inside);
  }
}
