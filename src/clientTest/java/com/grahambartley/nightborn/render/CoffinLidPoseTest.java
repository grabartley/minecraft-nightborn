package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinLidPoseTest {

  private static final float EPSILON = 1.0E-4F;

  @ParameterizedTest(name = "openness {0}: {1} degrees")
  @CsvSource({"0.0, 0.0", "1.0, 100.0", "0.5, 87.5"})
  @DisplayName("the lid eases out as it opens and rests at its widest")
  void easesOpen(final float openness, final float degrees) {
    assertEquals(degrees, CoffinLidPose.angle(openness), EPSILON);
  }

  static Stream<Arguments> headEnds() {
    return Stream.of(
        Arguments.of(Direction.NORTH, 0.5F, 0.0F),
        Arguments.of(Direction.EAST, 1.0F, 0.5F),
        Arguments.of(Direction.SOUTH, 0.5F, 1.0F),
        Arguments.of(Direction.WEST, 0.0F, 0.5F));
  }

  @ParameterizedTest(name = "facing {0}")
  @MethodSource("headEnds")
  @DisplayName("a shut lid turns with the coffin so its head end points the way the coffin faces")
  void turnsWithTheCoffin(final Direction facing, final float x, final float z) {
    final Vector4f headEnd = place(facing, 0.0F, 0.5F, 0.5F, 0.0F);
    assertEquals(x, headEnd.x(), EPSILON);
    assertEquals(0.5F, headEnd.y(), EPSILON);
    assertEquals(z, headEnd.z(), EPSILON);
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("an open lid swings up off the hinge side")
  void swingsUpOffTheHinge(final Direction facing) {
    final Vector4f farEdge = place(facing, 1.0F, 15.0F / 16.0F, 0.5F, 0.5F);
    final Vector4f hinge = place(facing, 1.0F, CoffinLidPose.HINGE_X, CoffinLidPose.HINGE_Y, 0.5F);
    assertTrue(farEdge.y() > 1.0F);
    assertEquals(CoffinLidPose.HINGE_Y, hinge.y(), EPSILON);
  }

  private static Vector4f place(
      final Direction facing, final float openness, final float x, final float y, final float z) {
    final MatrixStack matrices = new MatrixStack();
    CoffinLidPose.apply(matrices, facing, false, openness);
    return matrices.peek().getPositionMatrix().transform(new Vector4f(x, y, z, 1.0F));
  }

  static Stream<Arguments> standingCorners() {
    return Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
        .flatMap(
            facing ->
                Stream.of(
                        new Vector3f(1.0F / 16.0F, 9.0F / 16.0F, 0.0F),
                        new Vector3f(15.0F / 16.0F, 8.0F / 16.0F, 12.0F / 16.0F),
                        new Vector3f(6.0F / 16.0F, 10.0F / 16.0F, 2.0F))
                    .map(corner -> Arguments.of(facing, corner)));
  }

  @ParameterizedTest(name = "facing {0}, lid corner {1}")
  @MethodSource("standingCorners")
  @DisplayName("a shut lid on a standing coffin lands where the game turns the coffin's own model")
  void standsWithTheCoffin(final Direction facing, final Vector3f corner) {
    final MatrixStack matrices = new MatrixStack();
    CoffinLidPose.apply(matrices, facing, true, 0.0F);
    final Vector4f drawn =
        matrices
            .peek()
            .getPositionMatrix()
            .transform(new Vector4f(corner.x(), corner.y(), corner.z(), 1.0F));
    final Vector3f expected = turnedByTheBlockstate(corner, BLOCKSTATE_Y.get(facing));
    assertEquals(expected.x(), drawn.x(), EPSILON);
    assertEquals(expected.y(), drawn.y(), EPSILON);
    assertEquals(expected.z(), drawn.z(), EPSILON);
  }

  private static final Map<Direction, Integer> BLOCKSTATE_Y =
      Map.of(Direction.NORTH, 0, Direction.EAST, 90, Direction.SOUTH, 180, Direction.WEST, 270);

  private static Vector3f turnedByTheBlockstate(final Vector3f corner, final int y) {
    final Matrix4f rotation = ModelRotation.get(270, y).getRotation().getMatrix();
    final Vector4f turned =
        rotation.transform(
            new Vector4f(corner.x() - 0.5F, corner.y() - 0.5F, corner.z() - 0.5F, 1.0F));
    return new Vector3f(turned.x() + 0.5F, turned.y() + 0.5F, turned.z() + 0.5F);
  }
}
