package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseHandoverTest {

  @ParameterizedTest(name = "{0} shows {1}")
  @CsvSource({"nightborn:house_handover, nightborn:wheel/hand_over"})
  @DisplayName("handing the house over has its own action and its own wheel icon")
  void wheel(final String id, final String icon) {
    assertEquals(id, HouseHandover.ID.toString());
    assertEquals(icon, HouseHandover.ICON.toString());
  }
}
