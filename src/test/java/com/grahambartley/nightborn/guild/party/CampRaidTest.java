package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CampRaidTest {

  @ParameterizedTest(name = "night {0}, pitched {1}, rolled tonight {2}: roll {3}")
  @CsvSource({
    "true, true, false, true",
    "false, true, false, false",
    "true, false, false, false",
    "true, true, true, false"
  })
  void rollsForAnAttackOnceANightOnAPitchedCamp(
      final boolean night, final boolean pitched, final boolean rolled, final boolean due) {
    assertEquals(due, CampRaid.due(night, pitched, rolled));
  }

  @ParameterizedTest(name = "sleepers {0} blocks from the fire: {1} wake")
  @CsvSource({"'3.0, 5.0', '0, 1'", "'30.0, 2.0', '1'", "'24.0, 24.5', '0'"})
  void sleepersAtTheCampWake(final String distances, final String woken) {
    final List<Double> from =
        List.of(distances.split(", ")).stream().map(Double::parseDouble).toList();
    final List<Integer> expected =
        List.of(woken.split(", ")).stream().map(Integer::parseInt).toList();
    assertEquals(expected, CampRaid.wakers(from));
  }

  @ParameterizedTest(name = "{0} raiders near: over {1}")
  @CsvSource({"0, true", "1, false", "5, false"})
  void theRaidEndsWhenNoBatIsLeft(final int raiders, final boolean over) {
    assertEquals(over, CampRaid.over(raiders));
  }
}
