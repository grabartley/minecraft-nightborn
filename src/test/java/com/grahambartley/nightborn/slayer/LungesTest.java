package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LungesTest {

  @ParameterizedTest(name = "a stake's blow of {0} with {1} more: {2}")
  @CsvSource({"6, 0.5, 9", "4, 0, 4", "10, 1, 20"})
  @DisplayName("a lunge strikes with a stake's blow and its extra share")
  void blow(final float stakeBlow, final float share, final float blow) {
    assertEquals(blow, Lunges.blow(stakeBlow, share), 1.0E-6F);
  }
}
