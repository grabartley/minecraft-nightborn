package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisKindTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"entity\":\"nightborn:hunter\",\"keepsake\":\"nightborn:broken_stake\",\"epithets\":\"nightborn:nemesis_epithets\",\"boost\":0.25,\"max_boosts\":3,\"experience_times\":5,\"companions\":2,\"fight_end_seconds\":30}; 0.25; 3; 5; 2; 600",
        "{\"entity\":\"nightborn:vampire\",\"keepsake\":\"nightborn:lock_of_hair\",\"epithets\":\"nightborn:nemesis_epithets\",\"ages\":true}; 0.0; 0; 1; 0; 600",
        "{\"entity\":\"nightborn:vampire\",\"keepsake\":\"nightborn:lock_of_hair\",\"epithets\":\"nightborn:nemesis_epithets\",\"fight_end_seconds\":5}; 0.0; 0; 1; 0; 100"
      })
  @DisplayName("a kind reads its facts from data and falls back to plain defaults")
  void reads(
      final String json,
      final float boost,
      final int maxBoosts,
      final int times,
      final int companions,
      final int endTicks) {
    final NemesisKind kind =
        NemesisKind.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(boost, kind.boost(), 1e-6);
    assertEquals(maxBoosts, kind.maxBoosts());
    assertEquals(times, kind.experienceTimes());
    assertEquals(companions, kind.companions());
    assertEquals(endTicks, kind.fightEndTicks());
  }
}
