package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DaySkipTest {

  static Stream<Arguments> requirements() {
    return Stream.of(
        Arguments.of(1, 100, 1),
        Arguments.of(2, 100, 2),
        Arguments.of(3, 50, 2),
        Arguments.of(4, 50, 2),
        Arguments.of(5, 0, 1),
        Arguments.of(0, 100, 1));
  }

  @ParameterizedTest(name = "{0} vampires at {1}% need {2}")
  @MethodSource("requirements")
  @DisplayName("the share rounds up and never asks for fewer than one")
  void needsAShare(final int vampires, final int share, final int needed) {
    assertEquals(needed, DaySkip.required(vampires, share));
  }

  static Stream<Arguments> skips() {
    return Stream.of(
        Arguments.of(1, 2, 100, false),
        Arguments.of(2, 2, 100, true),
        Arguments.of(1, 2, 50, true),
        Arguments.of(0, 0, 100, false),
        Arguments.of(0, 3, 0, false),
        Arguments.of(1, 3, 0, true));
  }

  @ParameterizedTest(name = "{0} of {1} resting at {2}%: {3}")
  @MethodSource("skips")
  @DisplayName("the day passes only when enough vampires rest")
  void skipsWhenEnoughRest(
      final int resting, final int vampires, final int share, final boolean skipped) {
    assertEquals(skipped, DaySkip.skips(resting, vampires, share));
  }

  static Stream<Arguments> dusks() {
    return Stream.of(
        Arguments.of(0L, 13000L),
        Arguments.of(6000L, 13000L),
        Arguments.of(12999L, 13000L),
        Arguments.of(13000L, 37000L),
        Arguments.of(23500L, 37000L),
        Arguments.of(48000L + 1000L, 48000L + 13000L));
  }

  @ParameterizedTest(name = "{0} goes to {1}")
  @MethodSource("dusks")
  @DisplayName("the day always moves forward to the next dusk")
  void movesToTheNextDusk(final long time, final long dusk) {
    assertEquals(dusk, DaySkip.nextDusk(time));
  }
}
