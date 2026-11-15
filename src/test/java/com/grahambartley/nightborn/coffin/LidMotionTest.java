package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class LidMotionTest {

  @ParameterizedTest(name = "open {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a lid first seen holds its pose instead of swinging into it")
  void holdsThePoseItWasFoundIn(final boolean open) {
    final LidMotion lid = new LidMotion();
    lid.tick(open);
    assertEquals(open ? 1.0F : 0.0F, lid.openness(0.0F));
    assertEquals(open ? 1.0F : 0.0F, lid.openness(1.0F));
  }

  static Stream<Arguments> swings() {
    return Stream.of(
        Arguments.of(false, true, 1, 0.1F),
        Arguments.of(false, true, 5, 0.5F),
        Arguments.of(false, true, 10, 1.0F),
        Arguments.of(false, true, 20, 1.0F),
        Arguments.of(true, false, 3, 0.7F),
        Arguments.of(true, false, 15, 0.0F));
  }

  @ParameterizedTest(name = "from open {0} to open {1}, {2} ticks: {3}")
  @MethodSource("swings")
  @DisplayName("a lid swings a tenth of the way each tick and stops at the end")
  void swingsSteadily(final boolean from, final boolean to, final int ticks, final float openness) {
    final LidMotion lid = new LidMotion();
    lid.tick(from);
    for (int i = 0; i < ticks; i++) {
      lid.tick(to);
    }
    assertEquals(openness, lid.openness(1.0F), 1.0E-5F);
  }

  @ParameterizedTest(name = "partial tick {0}")
  @ValueSource(floats = {0.0F, 0.25F, 0.5F, 1.0F})
  @DisplayName("between ticks the lid is drawn part way along its last step")
  void blendsBetweenTicks(final float delta) {
    final LidMotion lid = new LidMotion();
    lid.tick(false);
    lid.tick(true);
    lid.tick(true);
    assertEquals(0.1F + delta * LidMotion.STEP, lid.openness(delta), 1.0E-5F);
  }
}
