package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WornWardTest {

  @ParameterizedTest(name = "holding {0}, vampire {1}, worn {2}: {3}")
  @CsvSource({
    "true, false, 0.0, 8.0",
    "false, false, 0.5, 4.0",
    "true, false, 0.5, 8.0",
    "false, false, 0.0, 0.0",
    "true, true, 0.5, 0.0",
    "false, true, 0.5, 0.0",
    "false, false, 2.0, 8.0"
  })
  @DisplayName("a worn crucifix wards at its share of the range, a held one at all of it")
  void reach(final boolean holding, final boolean vampire, final double worn, final double reach) {
    assertEquals(reach, WornWard.reach(holding, vampire, worn, 8.0), 1.0E-9);
  }
}
