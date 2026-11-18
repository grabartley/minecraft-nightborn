package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildTrialOrderTest {

  @ParameterizedTest(name = "{0} shows {1}")
  @CsvSource({"nightborn:guild_trial, nightborn:wheel/trials"})
  @DisplayName("taking a trial has its own action and its own wheel icon")
  void wheel(final String id, final String icon) {
    assertEquals(id, GuildTrialOrder.ID.toString());
    assertEquals(icon, GuildTrialOrder.ICON.toString());
  }
}
