package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faction.Faction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AccessoryTooltipTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "VAMPIRE, tooltip.nightborn.accessory.works_for.vampire",
    "HUMAN, tooltip.nightborn.accessory.works_for.human"
  })
  @DisplayName("the tooltip says who a piece works for")
  void key(final Faction wearer, final String key) {
    assertEquals(key, AccessoryTooltip.key(wearer));
  }
}
