package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PresenceTest {

  private static final Presence STRENGTH = new Presence(16.0F, Identifier.ofVanilla("strength"), 0);

  @ParameterizedTest(name = "leader fighting {0}, ally fighting {1}, {2} away: gains {3}")
  @CsvSource({
    "true, true, 4.0, true",
    "true, true, 16.0, true",
    "true, true, 16.5, false",
    "false, true, 4.0, false",
    "true, false, 4.0, false"
  })
  @DisplayName("an ally gains from a leader only while both fight and it stands near enough")
  void reaches(
      final boolean leader, final boolean ally, final double distance, final boolean gains) {
    assertEquals(gains, STRENGTH.reaches(leader, ally, distance));
  }

  @ParameterizedTest(name = "age {0}: pulses {1}")
  @CsvSource({"0, true", "1, false", "19, false", "20, true", "40, true"})
  @DisplayName("a leader's presence is felt once a second, and lingers past the next pulse")
  void pulses(final long age, final boolean pulses) {
    assertEquals(pulses, Presence.pulses(age));
    assertEquals(true, Presence.LINGER_TICKS > Presence.PULSE_TICKS);
  }

  @ParameterizedTest(name = "{0}: loads {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"radius\": 16, \"effect\": \"minecraft:strength\"} | true",
        "{\"radius\": 16, \"effect\": \"minecraft:strength\", \"amplifier\": 4} | true",
        "{\"radius\": 16, \"effect\": \"minecraft:strength\", \"amplifier\": 5} | false",
        "{\"radius\": 0.5, \"effect\": \"minecraft:strength\"} | false",
        "{\"radius\": 40, \"effect\": \"minecraft:strength\"} | false",
        "{\"effect\": \"minecraft:strength\"} | false"
      })
  @DisplayName("a presence needs a radius in range and an effect, with a small amplifier")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, Presence.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
