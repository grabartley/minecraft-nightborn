package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.rite.RiteSteps.Step;
import com.grahambartley.nightborn.guild.rite.RiteSteps.Way;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiteStepsTest {

  private static final GuildRite.Timing TIMING = new GuildRite.Timing(50, 28, 2, 200);

  @ParameterizedTest(name = "age {0}, left at {1}, way {2} -> {3}")
  @CsvSource({
    "0, -1, SEARCHING, CAST",
    "0, -1, READY, CAST",
    "49, -1, READY, CAST",
    "49, -1, NONE, CAST",
    "49, -1, UNSAFE, CAST",
    "50, -1, READY, BEGIN_LEAVE",
    "50, -1, NONE, FAIL_NONE",
    "50, -1, UNSAFE, FAIL_UNSAFE",
    "50, -1, SEARCHING, CAST",
    "50, -1, LOADING, CAST",
    "249, -1, SEARCHING, CAST",
    "249, -1, LOADING, CAST",
    "250, -1, SEARCHING, FAIL_NONE",
    "250, -1, LOADING, FAIL_UNSAFE",
    "100, -1, READY, BEGIN_LEAVE",
    "120, -1, READY, CAST",
    "150, -1, READY, BEGIN_LEAVE",
    "51, 50, READY, LEAVE",
    "77, 50, READY, LEAVE",
    "78, 50, READY, DEPART",
    "147, 120, READY, LEAVE",
    "148, 120, READY, DEPART"
  })
  @DisplayName("the cast plays out, then the hunter leaves once a way is found, or the rite fails")
  void at(final int age, final int leftAt, final Way way, final Step step) {
    assertEquals(step, RiteSteps.at(age, leftAt, TIMING, way));
  }

  @ParameterizedTest(name = "{0} ticks after arriving, sheltered {1} -> cue {2}, over {3}")
  @CsvSource({
    "0, true, false, false",
    "1, true, false, false",
    "2, true, true, false",
    "3, true, false, false",
    "60, true, false, false",
    "0, false, false, false",
    "1, false, false, false",
    "2, false, true, true",
    "61, false, false, true"
  })
  @DisplayName(
      "the arrival is shown once the hunter has settled, and the rite ends with the shelter")
  void arrival(
      final int sinceArrival, final boolean sheltered, final boolean cue, final boolean over) {
    assertEquals(cue, RiteSteps.settles(sinceArrival, TIMING));
    assertEquals(over, RiteSteps.over(sinceArrival, TIMING, sheltered));
  }

  @ParameterizedTest(name = "settling in {0} ticks, {1} ticks after arriving -> cue {2}")
  @CsvSource({
    "0, 0, true",
    "0, 1, false",
    "1, 0, false",
    "1, 1, true",
    "5, 5, true",
    "5, 4, false"
  })
  @DisplayName("the arrival is shown exactly once, even with no settling time")
  void settles(final int settleTicks, final int sinceArrival, final boolean cue) {
    assertEquals(
        cue, RiteSteps.settles(sinceArrival, new GuildRite.Timing(50, 28, settleTicks, 200)));
  }

  @ParameterizedTest(name = "age {0}, left at {1} -> casts again {2}, cast tick {3}")
  @CsvSource({
    "0, -1, false, 0",
    "49, -1, false, 49",
    "50, -1, true, 0",
    "75, -1, false, 25",
    "100, -1, true, 0",
    "100, 50, false, 0"
  })
  @DisplayName("while the way is sought the Guild Master casts the rite again, round after round")
  void rounds(final int age, final int leftAt, final boolean repeats, final int castTick) {
    assertEquals(repeats, RiteSteps.repeats(age, leftAt, TIMING));
    assertEquals(castTick, RiteSteps.castTick(age, TIMING));
  }
}
