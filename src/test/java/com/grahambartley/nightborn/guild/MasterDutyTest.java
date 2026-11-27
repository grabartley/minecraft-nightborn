package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MasterDutyTest {

  @ParameterizedTest(name = "swearing {0}, fighting {1}: breaks off {2}")
  @CsvSource({
    "false, false, false",
    "true, false, false",
    "false, true, false",
    "true, true, true"
  })
  @DisplayName("a Guild Master breaks off an oath when a fight finds him")
  void breaksOffAnOathToFight(
      final boolean swearing, final boolean fighting, final boolean breaks) {
    assertEquals(breaks, MasterDuty.breaksOath(swearing, fighting));
  }

  @ParameterizedTest(name = "vampire customer {0}, fighting {1}: closes {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("the counter closes for a vampire customer or when the Guild Master fights")
  void closesForAVampireOrAFight(
      final boolean vampireCustomer, final boolean fighting, final boolean closes) {
    assertEquals(closes, MasterDuty.closesCounter(vampireCustomer, fighting));
  }
}
