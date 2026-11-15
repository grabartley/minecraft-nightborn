package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CoatTest {

  private static final CoatingTerms TERMS = new CoatingTerms(3, 5, 200, 20, 60);

  @ParameterizedTest(name = "{1} hits used, started {2}, now {3}: {4} hits, {5} ticks, worn {6}")
  @CsvSource({
    "HOLY_WATER, 0, -1, 9000, 5, 200, false",
    "HOLY_WATER, 4, 100, 150, 1, 150, false",
    "GARLIC, 5, 100, 150, 0, 150, true",
    "GARLIC, 9, 100, 150, 0, 150, true",
    "GARLIC, 0, 100, 300, 5, 0, true",
    "HOLY_WATER, 0, 100, 299, 5, 1, false"
  })
  @DisplayName("a coat wears off after its hits or its time, whichever runs out first")
  void wears(
      final Coating coating,
      final int used,
      final long started,
      final long now,
      final int hits,
      final long ticks,
      final boolean worn) {
    final Coat coat = new Coat(coating, used, started);
    assertEquals(hits, coat.hitsLeft(TERMS));
    assertEquals(ticks, coat.ticksLeft(TERMS, now));
    assertEquals(worn, coat.worn(TERMS, now));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("the clock starts once, and each strike counts one hit")
  void startsAndStrikes(final Coating coating) {
    final Coat started = Coat.fresh(coating).startingAt(40L).startingAt(90L).struck().struck();
    assertEquals(new Coat(coating, 2, 40L), started);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("a coat saves and loads unchanged")
  void roundTrips(final Coating coating) {
    final Coat coat = new Coat(coating, 3, 1234L);
    assertEquals(
        coat,
        Coat.CODEC
            .parse(JsonOps.INSTANCE, Coat.CODEC.encodeStart(JsonOps.INSTANCE, coat).getOrThrow())
            .getOrThrow());
  }
}
