package com.grahambartley.nightborn.lord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RallyPowerTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"range\":32} | true",
        "{\"range\":1} | true",
        "{\"range\":0.5} | false",
        "{\"range\":49} | false",
        "{} | false"
      })
  @DisplayName("a rally reads how far the call carries, from 1 to 48 blocks")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, RallyPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "caster {0}, vampire {1}, helpless {2}, {3} away: {4}")
  @CsvSource({
    "false, true, false, 10.0, true",
    "false, true, false, 32.0, true",
    "false, true, false, 32.5, false",
    "true, true, false, 0.0, false",
    "false, false, false, 4.0, false",
    "false, true, true, 4.0, false"
  })
  @DisplayName("only other vampire mobs in reach that can still fight answer the call")
  void answers(
      final boolean caster,
      final boolean vampire,
      final boolean helpless,
      final double distance,
      final boolean answers) {
    assertEquals(answers, new RallyPower(32.0F).answers(caster, vampire, helpless, distance));
  }
}
