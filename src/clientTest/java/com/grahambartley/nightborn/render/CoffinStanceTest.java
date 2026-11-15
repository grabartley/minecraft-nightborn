package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinStanceTest {

  private static final float PIXEL = 1.0F / 16.0F;

  static Stream<Arguments> points() {
    return Stream.of(
        Arguments.of("foot end", false, new Vector3f(0, 0, 24 * PIXEL), new Vector3f(0, 0, 1.5F)),
        Arguments.of(
            "foot end", true, new Vector3f(0, 0, 24 * PIXEL), new Vector3f(0, -1.0F, -0.5F)),
        Arguments.of("floor", true, new Vector3f(0, 0, 0), new Vector3f(0, 0.5F, -0.5F)),
        Arguments.of("lid top", true, new Vector3f(0, 12 * PIXEL, 0), new Vector3f(0, 0.5F, 0.25F)),
        Arguments.of(
            "head end", true, new Vector3f(0, 0, -16 * PIXEL), new Vector3f(0, 1.5F, -0.5F)));
  }

  @ParameterizedTest(name = "{0}, standing {1}")
  @MethodSource("points")
  @DisplayName(
      "a standing coffin turns upright about the head cell: foot down, back to the wall, lid out")
  void standsUpright(
      final String part, final boolean standing, final Vector3f model, final Vector3f expected) {
    final MatrixStack matrices = new MatrixStack();
    CoffinStance.apply(matrices, standing);
    final Vector3f placed = matrices.peek().getPositionMatrix().transformPosition(model);
    assertEquals(expected.x, placed.x, 1.0E-5F);
    assertEquals(expected.y, placed.y, 1.0E-5F);
    assertEquals(expected.z, placed.z, 1.0E-5F);
  }
}
