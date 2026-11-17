package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlurryAttackTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"hits\":3,\"every_ticks\":8,\"damage\":7} | true",
        "{\"hits\":3,\"every_ticks\":8,\"damage\":7,\"reach\":3.5,\"lunge\":0.35} | true",
        "{\"hits\":0,\"every_ticks\":8,\"damage\":7} | false",
        "{\"hits\":13,\"every_ticks\":8,\"damage\":7} | false",
        "{\"hits\":3,\"every_ticks\":0,\"damage\":7} | false",
        "{\"hits\":3,\"damage\":7} | false",
        "{\"hits\":3,\"every_ticks\":8,\"damage\":7,\"reach\":0.5} | false"
      })
  @DisplayName("a flurry needs between one and twelve hits, a beat and a damage")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads,
        FlurryAttack.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} hits every {1} ticks, tick {2}: hits {3}")
  @CsvSource({
    "3, 8, 0, true",
    "3, 8, 7, false",
    "3, 8, 8, true",
    "3, 8, 16, true",
    "3, 8, 24, false",
    "1, 5, 0, true",
    "1, 5, 5, false"
  })
  @DisplayName("a flurry lands on its beat, the first blow at once, and stops after its last hit")
  void hitsAt(final int hits, final int every, final int tick, final boolean lands) {
    final FlurryAttack flurry = new FlurryAttack(hits, every, 7.0F, 3.0F, 0.4F);
    assertEquals(lands, flurry.hitsAt(tick));
    assertEquals(hits * every, flurry.ticks());
  }

  @ParameterizedTest(name = "facing ({0}, {1}), foe at ({2}, {3}): reached {4}")
  @CsvSource({
    "0, 1, 0, 2, true",
    "0, 1, 0, 3, true",
    "0, 1, 0, 3.1, false",
    "0, 1, 2, 2, true",
    "0, 1, 2, 0.1, false",
    "0, 1, 0, -2, false",
    "1, 0, 2, 0, true",
    "1, 0, -2, 0, false",
    "0, 1, 0, 0, true"
  })
  @DisplayName("a flurry reaches foes in front of the boss and within its reach, never behind")
  void reaches(
      final double facingX,
      final double facingZ,
      final double toX,
      final double toZ,
      final boolean reached) {
    final FlurryAttack flurry = new FlurryAttack(3, 8, 7.0F, 3.0F, 0.4F);
    assertEquals(reached, flurry.reaches(facingX, facingZ, toX, toZ));
  }
}
