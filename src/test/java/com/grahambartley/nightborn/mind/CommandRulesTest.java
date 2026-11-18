package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandRulesTest {

  @ParameterizedTest(name = "player {0}, mesmerised by caster {1}, boss {2}: commandable {3}")
  @CsvSource({
    "false, true, false, true",
    "true, true, false, false",
    "false, false, false, false",
    "false, true, true, false"
  })
  @DisplayName(
      "only a creature the caster holds mesmerised can be commanded, never a player or boss")
  void commandable(
      final boolean player, final boolean held, final boolean boss, final boolean commandable) {
    assertEquals(commandable, CommandRules.commandable(player, held, boss));
  }

  @ParameterizedTest(name = "distance {0}, now {1}, last {2}: strikes {3}")
  @CsvSource({"1.5, 100, 70, true", "1.5, 100, 90, false", "3.0, 100, 0, false"})
  @DisplayName("a commanded creature strikes when close, once a second")
  void strikes(final double distance, final long now, final long last, final boolean strikes) {
    assertEquals(strikes, CommandRules.strikes(distance, now, last));
  }

  @ParameterizedTest(name = "distance {0}, gap {1}: closes in {2}")
  @CsvSource({"5.0, 3.0, true", "2.0, 3.0, false"})
  @DisplayName("a commanded creature walks only while too far away")
  void closes(final double distance, final double gap, final boolean closes) {
    assertEquals(closes, CommandRules.closesIn(distance, gap));
  }
}
