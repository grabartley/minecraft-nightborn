package com.grahambartley.nightborn.client.mirror;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MirrorGlassTest {

  @ParameterizedTest(name = "{0}x{1} screen")
  @CsvSource({"320, 240", "427, 240", "854, 480", "74, 96"})
  @DisplayName("the glass sits in the middle of the screen inside its frame")
  void centres(final int width, final int height) {
    final MirrorGlass glass = MirrorGlass.centred(width, height);
    assertEquals(MirrorGlass.WIDTH, glass.right() - glass.left());
    assertEquals(MirrorGlass.HEIGHT, glass.bottom() - glass.top());
    assertEquals((width - MirrorGlass.WIDTH) / 2, glass.left());
    assertEquals((height - MirrorGlass.HEIGHT) / 2, glass.top());
    assertEquals(glass.left() + MirrorGlass.FRAME, glass.glassLeft());
    assertEquals(glass.top() + MirrorGlass.FRAME, glass.glassTop());
    assertEquals(glass.right() - MirrorGlass.FRAME, glass.glassRight());
    assertEquals(glass.bottom() - MirrorGlass.FRAME, glass.glassBottom());
  }
}
