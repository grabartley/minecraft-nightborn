package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseInviteTest {

  @ParameterizedTest(name = "{0} shows {1}")
  @CsvSource({"nightborn:house_invite, nightborn:wheel/invite"})
  @DisplayName("the invitation has its own action and its own wheel icon")
  void wheel(final String id, final String icon) {
    assertEquals(id, HouseInvite.ID.toString());
    assertEquals(icon, HouseInvite.ICON.toString());
  }
}
