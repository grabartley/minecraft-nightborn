package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RescuesTest {

  @ParameterizedTest(name = "below {0}, health {1}/{2}: due {3}")
  @CsvSource({
    "0.25, 4, 20, true",
    "0.25, 5, 20, false",
    "0.25, 10, 20, false",
    ", 1, 20, false",
    "0.25, 1, 0, false"
  })
  @DisplayName("a rescuing power wakes only once health falls under its line")
  void due(final Float below, final float health, final float most, final boolean due) {
    assertEquals(due, Rescues.due(Optional.ofNullable(below), health, most));
  }
}
