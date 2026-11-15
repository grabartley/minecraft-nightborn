package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinLidTest {

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(true, false, true, false),
        Arguments.of(false, false, true, false),
        Arguments.of(false, true, false, true),
        Arguments.of(true, true, false, true),
        Arguments.of(true, false, false, true),
        Arguments.of(false, false, false, false),
        Arguments.of(false, true, true, false),
        Arguments.of(true, true, true, true));
  }

  @ParameterizedTest(name = "open {0}, occupied {1} then {2}: open {3}")
  @MethodSource("changes")
  @DisplayName("the lid shuts over a sleeper, opens when they rise, and is left alone otherwise")
  void followsTheSleeper(
      final boolean open, final boolean was, final boolean now, final boolean expected) {
    assertEquals(expected, CoffinLid.openAfter(open, was, now));
  }
}
