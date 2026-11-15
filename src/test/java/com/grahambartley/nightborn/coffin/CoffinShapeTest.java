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
    final Box box = CoffinShape.of(part, facing).getBoundingBox();
    assertTrue(box.minX >= 0 && box.minZ >= 0 && box.maxX <= 1 && box.maxZ <= 1);
    assertEquals(0.0, box.minY, 1.0E-9);
    assertEquals(CoffinShape.HEIGHT * PIXEL, box.maxY, 1.0E-9);
  }

  static Stream<Arguments> widths() {
    return Stream.of(
        Arguments.of(BedPart.HEAD, Direction.NORTH, 1, 15),
        Arguments.of(BedPart.FOOT, Direction.NORTH, 2, 14),
        Arguments.of(BedPart.HEAD, Direction.EAST, 1, 15),
        Arguments.of(BedPart.FOOT, Direction.WEST, 2, 14));
  }

  @ParameterizedTest(name = "{0} facing {1} spans {2}..{3}")
  @MethodSource("widths")
  @DisplayName("the shoulders sit in the head and the body narrows towards the foot")
  void widensAtTheShoulders(
      final BedPart part, final Direction facing, final int from, final int to) {
    final Box box = CoffinShape.of(part, facing).getBoundingBox();
    final boolean alongX = facing.getAxis() == Direction.Axis.Z;
    assertEquals(from * PIXEL, alongX ? box.minX : box.minZ, 1.0E-9);
    assertEquals(to * PIXEL, alongX ? box.maxX : box.maxZ, 1.0E-9);
  }

  static Stream<Arguments> ends() {
    return Stream.of(
        Arguments.of(BedPart.HEAD, 6.0 / 16.0, 0.5 / 16.0, true),
        Arguments.of(BedPart.HEAD, 2.0 / 16.0, 0.5 / 16.0, false),
        Arguments.of(BedPart.FOOT, 8.0 / 16.0, 15.5 / 16.0, true),
        Arguments.of(BedPart.FOOT, 4.5 / 16.0, 15.5 / 16.0, false));
  }

  @ParameterizedTest(name = "{0} at x {1}, z {2}: solid {3}")
  @MethodSource("ends")
  @DisplayName("the head and foot ends are narrower than the shoulders")
  void tapersAtTheEnds(final BedPart part, final double x, final double z, final boolean solid) {
    final VoxelShape shape = CoffinShape.of(part, Direction.NORTH);
    final boolean inside =
        shape.getBoundingBoxes().stream()
            .anyMatch(box -> x > box.minX && x < box.maxX && z > box.minZ && z < box.maxZ);
    assertEquals(solid, inside);
    assertFalse(shape.isEmpty());
  }
}
