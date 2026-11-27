package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterTargetsTest {

  @ParameterizedTest(name = "vampire {0}, camp raider {1}, exempt {2}: hunted {3}")
  @CsvSource({
    "true, false, false, true",
    "true, false, true, false",
    "false, true, false, true",
    "false, true, true, false",
    "false, false, false, false",
    "false, false, true, false"
  })
  void huntsVampiresAndCampRaidersAndLeavesHumansAlone(
      final boolean vampire, final boolean raider, final boolean exempt, final boolean hunted) {
    assertEquals(hunted, HunterTargets.hunts(vampire, raider, exempt));
  }

  @ParameterizedTest(name = "{0} blocks squared, near {1}, chance up {2}: notices {3}")
  @CsvSource({
    "4, 8, false, true",
    "64, 8, false, true",
    "65, 8, false, false",
    "900, 8, true, true",
    "900, 8, false, false"
  })
  void noticesANearVampireAtOnceAndAFarOneByChance(
      final double distanceSquared,
      final double near,
      final boolean chance,
      final boolean notices) {
    assertEquals(notices, HunterTargets.noticesNow(distanceSquared, near, chance));
  }
}
