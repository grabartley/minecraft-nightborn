package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RestTallyTest {

  static Stream<Arguments> tallies() {
    return Stream.of(
        Arguments.of(new RestTally(1, 2), null, true),
        Arguments.of(new RestTally(1, 2), new RestTally(0, 2), true),
        Arguments.of(new RestTally(2, 2), new RestTally(1, 2), true),
        Arguments.of(new RestTally(1, 3), new RestTally(1, 2), true),
        Arguments.of(new RestTally(1, 2), new RestTally(1, 2), false),
        Arguments.of(new RestTally(0, 2), new RestTally(1, 2), false),
        Arguments.of(new RestTally(0, 1), null, false));
  }

  @ParameterizedTest(name = "{0} after {1}: tells {2}")
  @MethodSource("tallies")
  @DisplayName("vampires hear the tally whenever it changes while anyone rests")
  void announcesChanges(final RestTally now, final RestTally before, final boolean tells) {
    assertEquals(tells, now.announces(before));
  }
}
