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
    "120, -1, READY, BEGIN_LEAVE",
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
}
