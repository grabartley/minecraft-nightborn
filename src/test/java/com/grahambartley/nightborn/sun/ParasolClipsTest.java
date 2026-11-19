package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.RawAnimation;

class ParasolClipsTest {

  static Stream<Arguments> clips() {
    return Stream.of(
        Arguments.of("nothing yet", null, true, ParasolClips.OPENED),
        Arguments.of("nothing yet", null, false, ParasolClips.CLOSED),
        Arguments.of("held open", ParasolClips.OPENED, true, ParasolClips.OPENED),
        Arguments.of("held closed", ParasolClips.CLOSED, false, ParasolClips.CLOSED),
        Arguments.of("held closed", ParasolClips.CLOSED, true, ParasolClips.OPENING),
        Arguments.of("held open", ParasolClips.OPENED, false, ParasolClips.CLOSING),
        Arguments.of("opening", ParasolClips.OPENING, true, ParasolClips.OPENING),
        Arguments.of("closing", ParasolClips.CLOSING, false, ParasolClips.CLOSING),
        Arguments.of("opening", ParasolClips.OPENING, false, ParasolClips.CLOSING),
        Arguments.of("closing", ParasolClips.CLOSING, true, ParasolClips.OPENING),
        Arguments.of(
            "an equal open clip",
            RawAnimation.begin().thenPlayAndHold("open"),
            true,
            RawAnimation.begin().thenPlayAndHold("open")));
  }

  @ParameterizedTest(name = "{0}, open {2}")
  @MethodSource("clips")
  @DisplayName("a parasol plays its open or close clip only when its state changes")
  void next(
      final String playing,
      final RawAnimation current,
      final boolean open,
      final RawAnimation expected) {
    assertEquals(expected, ParasolClips.next(current, open));
  }
}
