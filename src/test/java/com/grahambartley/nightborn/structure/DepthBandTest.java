package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DepthBandTest {

  @ParameterizedTest(name = "{0} to {1} inside a world of {2} to {3} is {4} to {5}, {6} tall")
  @CsvSource({
    "-50, -10, -64, 320, -50, -10, 40",
    "-80, -10, -64, 320, -64, -10, 54",
    "300, 340, -64, 320, 300, 320, 20",
    "400, 420, -64, 320, 400, 320, 0"
  })
  void staysInsideTheWorld(
      final int bottom,
      final int top,
      final int worldBottom,
      final int worldTop,
      final int fittedBottom,
      final int fittedTop,
      final int height) {
    final DepthBand fitted = new DepthBand(bottom, top).within(worldBottom, worldTop);
    assertEquals(new DepthBand(fittedBottom, fittedTop), fitted);
    assertEquals(height, fitted.height());
  }
}
