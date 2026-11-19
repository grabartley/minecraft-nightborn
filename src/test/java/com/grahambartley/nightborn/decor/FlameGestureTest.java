package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlameGestureTest {

  @ParameterizedTest(name = "lights {0}: {1}")
  @CsvSource({"true, LIGHT_CANDLE", "false, DOUSE_CANDLE"})
  @DisplayName("lighting and putting out a flame each have their own gesture")
  void of(final boolean lights, final VampirePose pose) {
    assertEquals(pose, FlameGesture.of(lights));
  }

  @ParameterizedTest(name = "tended {0}, lit before {1}, lit after {2}: {3}")
  @CsvSource({
    "true, false, true, LIGHT_CANDLE",
    "true, true, false, DOUSE_CANDLE",
    "true, true, true, ",
    "true, false, false, ",
    "false, false, true, ",
    "false, true, false, "
  })
  @DisplayName(
      "a vampire gestures only when it actually lit or put out a candle, candelabra or"
          + " chandelier")
  void changed(
      final boolean tended, final boolean before, final boolean after, final VampirePose pose) {
    assertEquals(Optional.ofNullable(pose), FlameGesture.changed(tended, before, after));
  }
}
