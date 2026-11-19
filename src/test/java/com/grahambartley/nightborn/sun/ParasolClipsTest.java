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
        Arguments.of("nothing yet", null, true, true, ParasolClips.OPENED),
        Arguments.of("nothing yet", null, false, true, ParasolClips.CLOSED),
        Arguments.of("held open", ParasolClips.OPENED, true, true, ParasolClips.OPENED),
        Arguments.of("held closed", ParasolClips.CLOSED, false, true, ParasolClips.CLOSED),
        Arguments.of("held closed", ParasolClips.CLOSED, true, true, ParasolClips.OPENING),
        Arguments.of("held open", ParasolClips.OPENED, false, true, ParasolClips.CLOSING),
        Arguments.of("opening", ParasolClips.OPENING, true, true, ParasolClips.OPENING),
        Arguments.of("closing", ParasolClips.CLOSING, false, true, ParasolClips.CLOSING),
        Arguments.of("opening", ParasolClips.OPENING, false, true, ParasolClips.CLOSING),
        Arguments.of("closing", ParasolClips.CLOSING, true, true, ParasolClips.OPENING),
        Arguments.of(
            "an equal open clip",
            RawAnimation.begin().thenPlayAndHold("open"),
            true,
            true,
            RawAnimation.begin().thenPlayAndHold("open")),
        Arguments.of("shared, held open", ParasolClips.OPENED, false, false, ParasolClips.CLOSED),
        Arguments.of("shared, held closed", ParasolClips.CLOSED, true, false, ParasolClips.OPENED),
        Arguments.of("shared, opening", ParasolClips.OPENING, true, false, ParasolClips.OPENED));
  }

  @ParameterizedTest(name = "{0}, open {2}, own state {3}")
  @MethodSource("clips")
  @DisplayName("a parasol plays its open or close clip only when its state changes")
  void next(
      final String playing,
      final RawAnimation current,
      final boolean open,
      final boolean ownState,
      final RawAnimation expected) {
    assertEquals(expected, ParasolClips.next(current, open, ownState));
  }
}
