package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrainAttackTest {

  private static final DrainAttack DRAIN = new DrainAttack(120, 10, 2.0F, 6.0F, 14.0F, 18.0F);

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"ticks\":120,\"damage\":2,\"heal\":6,\"break_damage\":18} | true",
        "{\"ticks\":120,\"every_ticks\":10,\"damage\":2,\"heal\":6,\"range\":14,\"break_damage\":18} | true",
        "{\"damage\":2,\"heal\":6,\"break_damage\":18} | false",
        "{\"ticks\":120,\"damage\":2,\"heal\":6} | false",
        "{\"ticks\":0,\"damage\":2,\"heal\":6,\"break_damage\":18} | false",
        "{\"ticks\":120,\"every_ticks\":0,\"damage\":2,\"heal\":6,\"break_damage\":18} | false"
      })
  @DisplayName("a drain needs a length, a damage, a heal and the damage that breaks it")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, DrainAttack.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "tick {0}, {1} taken, foe alive {2}, {3} blocks away: {4}")
  @CsvSource({
    "0, 0, true, 5, HOLD",
    "5, 0, true, 5, HOLD",
    "10, 0, true, 5, PULSE",
    "110, 17.9, true, 14, PULSE",
    "10, 18, true, 5, BROKEN",
    "3, 40, false, 50, BROKEN",
    "120, 0, true, 5, DONE",
    "10, 0, false, 5, DONE",
    "10, 0, true, 14.1, DONE"
  })
  @DisplayName(
      "a drain pulses on its beat until it runs out, loses its foe or takes enough damage to break")
  void steps(
      final int tick,
      final float taken,
      final boolean alive,
      final double distance,
      final DrainAttack.Step step) {
    assertEquals(step, DRAIN.step(tick, taken, alive, distance));
  }

  @ParameterizedTest(name = "{0} health healing {1} under a ceiling of {2}: {3}")
  @CsvSource({"50, 6, 70, 56", "66, 6, 70, 70", "70, 6, 70, 70", "80, 6, 70, 80", "50, 0, 70, 50"})
  @DisplayName("a drain heals up to the top of the current phase and never takes health away")
  void heals(final float health, final float heal, final float ceiling, final float healed) {
    assertEquals(healed, DrainAttack.healed(health, heal, ceiling), 1.0E-6F);
  }
}
