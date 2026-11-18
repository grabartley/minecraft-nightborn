package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseLeaveTest {

  @ParameterizedTest(name = "{0} shows {1}")
  @CsvSource({"nightborn:house_leave, nightborn:wheel/leave"})
  @DisplayName("leaving the house has its own action and its own wheel icon")
  void wheel(final String id, final String icon) {
    assertEquals(id, HouseLeave.ID.toString());
    assertEquals(icon, HouseLeave.ICON.toString());
  }
}
