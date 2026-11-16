package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterTargetsTest {

  @ParameterizedTest(name = "vampire {0}, exempt {1}: hunted {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  void huntsVampiresAndLeavesHumansAlone(
      final boolean vampire, final boolean exempt, final boolean hunted) {
    assertEquals(hunted, HunterTargets.hunts(vampire, exempt));
  }
}
