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
    final Coat coat = new Coat(coating, used, started, 0);
    assertEquals(hits, coat.hitsLeft(TERMS));
    assertEquals(ticks, coat.ticksLeft(TERMS, now));
    assertEquals(worn, coat.worn(TERMS, now));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("the clock starts once, and each strike counts one hit")
  void startsAndStrikes(final Coating coating) {
    final Coat started = Coat.fresh(coating).startingAt(40L).startingAt(90L).struck().struck();
    assertEquals(new Coat(coating, 2, 40L, 0), started);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("a coat saves and loads unchanged")
  void roundTrips(final Coating coating) {
    final Coat coat = new Coat(coating, 3, 1234L, 0);
    assertEquals(
        coat,
        Coat.CODEC
            .parse(JsonOps.INSTANCE, Coat.CODEC.encodeStart(JsonOps.INSTANCE, coat).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0} used, started {1}, now {2}, ammo {3}: live {4}")
  @CsvSource({
    "0, -1, 9000, false, true",
    "5, 100, 150, false, false",
    "0, 100, 300, false, false",
    "5, 100, 300, true, true"
  })
  @DisplayName("a worn coat is spent on a weapon, and ammo keeps its coat until fired")
  void live(
      final int used, final long started, final long now, final boolean ammo, final boolean live) {
    assertEquals(live, new Coat(Coating.GARLIC, used, started, 0).live(TERMS, now, ammo));
  }

  @ParameterizedTest(name = "{0} used, started {1}, strike at {2}: {3}")
  @CsvSource({"0, -1, 50, '1, 50'", "3, 10, 50, '4, 10'", "4, 10, 50, ''", "0, 10, 210, ''"})
  @DisplayName("a strike starts the clock, counts a hit, and wears the coat off on its last hit")
  void afterStrike(final int used, final long started, final long now, final String left) {
    assertEquals(
        expected(left), new Coat(Coating.HOLY_WATER, used, started, 0).afterStrike(TERMS, now));
  }

  @ParameterizedTest(name = "{0} used, started {1}, checked at {2}: {3}")
  @CsvSource({"0, -1, 50, '0, 50'", "2, 10, 50, '2, 10'", "0, 10, 210, ''", "5, 10, 50, ''"})
  @DisplayName("an inventory check starts the clock once and drops a worn coat")
  void afterCheck(final int used, final long started, final long now, final String left) {
    assertEquals(
        expected(left), new Coat(Coating.HOLY_WATER, used, started, 0).afterCheck(TERMS, now));
  }

  @ParameterizedTest(name = "{0}, bonus {1}, lasts x{2}: bonus {3}")
  @CsvSource({
    "HOLY_WATER, 0, 1.25, 50",
    "HOLY_WATER, 0, 1.0, 0",
    "HOLY_WATER, 30, 1.25, 30",
    "GARLIC, 0, 1.25, 0"
  })
  @DisplayName("a blessing is stamped once with the holder's extra time, and garlic never is")
  void stretchedBy(final Coating coating, final int bonus, final double lasts, final int stamped) {
    assertEquals(stamped, new Coat(coating, 0, 100L, bonus).stretchedBy(TERMS, lasts).bonusTicks());
  }

  @ParameterizedTest(name = "bonus {0}, now {1}: {2} ticks left")
  @CsvSource({"0, 250, 50", "50, 250, 100", "50, 350, 0"})
  @DisplayName("a stamped blessing outlasts a plain one by its bonus")
  void bonusLasts(final int bonus, final long now, final long left) {
    assertEquals(left, new Coat(Coating.HOLY_WATER, 0, 100L, bonus).ticksLeft(TERMS, now));
  }

  @ParameterizedTest(name = "blessed for {0}, {1} hits struck, now {2}: {3} ticks left, worn {4}")
  @CsvSource({
    "1200, 0, 100, 1200, false",
    "1200, 50, 700, 600, false",
    "1200, 500, 1299, 1, false",
    "1200, 0, 1300, 0, true"
  })
  @DisplayName("a blessed weapon has no limit on its hits and wears off only with time")
  void blessing(
      final int ticks, final int struck, final long now, final long left, final boolean worn) {
    Coat coat = Coat.blessing(100L, ticks);
    for (int hit = 0; hit < struck; hit++) {
      coat = coat.struck();
    }
    assertEquals(true, coat.blessed());
    assertEquals(Coating.HOLY_WATER, coat.coating());
    assertEquals(left, coat.ticksLeft(TERMS, now));
    assertEquals(worn, coat.worn(TERMS, now));
  }

  @ParameterizedTest(name = "blessed for {0}, lasts x{1}: bonus {2}")
  @CsvSource({"1200, 1.25, 300", "1200, 1.0, 0", "600, 1.25, 150"})
  @DisplayName("the holder's extra blessing time stretches a blessed weapon by its own length")
  void blessingStretched(final int ticks, final double lasts, final int bonus) {
    assertEquals(bonus, Coat.blessing(100L, ticks).stretchedBy(TERMS, lasts).bonusTicks());
  }

  @ParameterizedTest(name = "blessed for {0}")
  @CsvSource({"1200", "1"})
  @DisplayName("a blessed weapon saves and loads unchanged")
  void blessingRoundTrips(final int ticks) {
    final Coat coat = Coat.blessing(500L, ticks);
    assertEquals(
        coat,
        Coat.CODEC
            .parse(JsonOps.INSTANCE, Coat.CODEC.encodeStart(JsonOps.INSTANCE, coat).getOrThrow())
            .getOrThrow());
  }

  private static java.util.Optional<Coat> expected(final String left) {
    if (left.isEmpty()) {
      return java.util.Optional.empty();
    }
    final String[] parts = left.split(", ");
    return java.util.Optional.of(
        new Coat(Coating.HOLY_WATER, Integer.parseInt(parts[0]), Long.parseLong(parts[1]), 0));
  }
}
