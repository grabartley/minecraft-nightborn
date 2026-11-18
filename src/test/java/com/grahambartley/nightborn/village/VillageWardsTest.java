package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageWardsTest {

  @ParameterizedTest(name = "{0} with wards up {1}, hung tonight {2}: hang {3}")
  @CsvSource({
    "CALM, false, false, false",
    "WARY, false, false, false",
    "WARDED, false, false, true",
    "WARDED, true, false, false",
    "WARDED, false, true, false",
    "HUNTED, false, false, true"
  })
  @DisplayName(
      "wards go up at Warded, at most once a night, so taken wards are not hung again at once")
  void hangs(
      final VillageStep step, final boolean hung, final boolean tonight, final boolean hangs) {
    assertEquals(hangs, VillageWards.hangs(step, hung, tonight));
  }

  @ParameterizedTest(name = "{0} with wards up {1}: take down {2}")
  @CsvSource({
    "CALM, true, true",
    "WARY, true, true",
    "WARDED, true, false",
    "HUNTED, true, false",
    "CALM, false, false"
  })
  @DisplayName("wards come down when the village falls below Warded")
  void takesDown(final VillageStep step, final boolean hung, final boolean takesDown) {
    assertEquals(takesDown, VillageWards.takesDown(step, hung));
  }
}
