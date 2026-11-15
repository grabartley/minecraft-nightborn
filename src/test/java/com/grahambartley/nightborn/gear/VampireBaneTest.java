package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.GearSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireBaneTest {

  private static final GearSettings SETTINGS = new GearSettings(4, 3);

  @ParameterizedTest(name = "vampire {0}, stake {1}, silver {2}: +{3}")
  @CsvSource({
    "true, true, false, 4",
    "true, false, true, 3",
    "true, true, true, 7",
    "true, false, false, 0",
    "false, true, true, 0",
    "false, false, true, 0"
  })
  @DisplayName("stakes and silver add their damage against vampires only")
  void bonus(final boolean vampire, final boolean stake, final boolean silver, final float bonus) {
    assertEquals(bonus, VampireBane.bonus(vampire, stake, silver, SETTINGS));
  }
}
