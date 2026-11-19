package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SixthSenseTest {

  @ParameterizedTest(name = "vampire mob {0}, targets {1}, watches {2}, stalks {3} -> {4}")
  @CsvSource({
    "true, true, false, false, true",
    "true, false, true, false, true",
    "true, false, false, true, true",
    "true, false, false, false, false",
    "false, true, true, true, false"
  })
  @DisplayName("only a vampire mob that targets, watches or stalks the hunter sets it off")
  void warns(
      final boolean vampireMob,
      final boolean targets,
      final boolean watches,
      final boolean stalks,
      final boolean warns) {
    assertEquals(warns, SixthSense.warns(vampireMob, targets, watches, stalks));
  }
}
