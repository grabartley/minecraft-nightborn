package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageWardsTest {

  @ParameterizedTest(name = "{0} with wards up {1}: hang {2}, take down {3}")
  @CsvSource({
    "CALM, false, false, false",
    "WARY, false, false, false",
    "WARY, true, false, true",
    "WARDED, false, true, false",
    "WARDED, true, false, false",
    "HUNTED, false, true, false",
    "CALM, true, false, true"
  })
  @DisplayName("wards go up at Warded and come down when the village falls below it")
  void hangsAndTakesDown(
      final VillageStep step, final boolean hung, final boolean hangs, final boolean takesDown) {
    assertEquals(hangs, VillageWards.hangs(step, hung));
    assertEquals(takesDown, VillageWards.takesDown(step, hung));
  }
}
