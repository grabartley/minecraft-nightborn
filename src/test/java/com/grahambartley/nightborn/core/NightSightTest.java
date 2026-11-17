package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NightSightTest {

  @ParameterizedTest(name = "{0} ticks left: refresh {1}")
  @CsvSource({"0, true", "1, true", "219, true", "220, false", "400, false"})
  @DisplayName("night sight is topped up before it starts to flicker, and not every tick")
  void refreshesBeforeTheFlicker(final int ticksLeft, final boolean refresh) {
    assertEquals(refresh, NightSight.needsRefresh(ticksLeft));
  }

  @ParameterizedTest(name = "night {0}, dark sight {1}: sees {2}")
  @CsvSource({"true, false, true", "false, false, false", "false, true, true", "true, true, true"})
  @DisplayName("a vampire sees in the dark at night, and at any hour with dark sight")
  void sees(final boolean night, final boolean darkSight, final boolean sees) {
    assertEquals(sees, NightSight.sees(night, darkSight));
  }
}
