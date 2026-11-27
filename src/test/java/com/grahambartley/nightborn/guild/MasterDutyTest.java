package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MasterDutyTest {

  @ParameterizedTest(name = "trading {0}, swearing {1}: watches {2}")
  @CsvSource({
    "false, false, true",
    "true, false, false",
    "false, true, false",
    "true, true, false"
  })
  @DisplayName("a Guild Master looks for foes only while he neither trades nor swears anyone in")
  void watchesOnlyWhenFree(final boolean trading, final boolean swearing, final boolean watches) {
    assertEquals(watches, MasterDuty.watches(trading, swearing));
  }

  @ParameterizedTest(name = "vampire customer {0}, fighting {1}: closes {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("the counter closes for a vampire customer or when the Guild Master fights")
  void closesForAVampireOrAFight(
      final boolean vampireCustomer, final boolean fighting, final boolean closes) {
    assertEquals(closes, MasterDuty.closesCounter(vampireCustomer, fighting));
  }
}
