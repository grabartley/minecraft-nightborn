package com.grahambartley.nightborn.weakened;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeakenedSourcesTest {

  @ParameterizedTest(name = "{0} + {1} + {2} = {3}")
  @CsvSource({"10, 0, 0, 10", "30, 20, 10, 60", "60, 50, 0, 100", "90, 90, 90, 100"})
  @DisplayName("different sources add up, to the cap")
  void addUpToTheCap(final int a, final int b, final int c, final int level) {
    final WeakenedSources sources =
        WeakenedSources.NONE.with("garlic", a, 100).with("cross", b, 100).with("holy", c, 100);
    assertEquals(level, sources.level(false));
  }

  @Test
  @DisplayName("the same source twice keeps the stronger and the longer, it does not stack")
  void sameSourceDoesNotStack() {
    final WeakenedSources sources =
        WeakenedSources.NONE.with("garlic", 20, 100).with("garlic", 10, 300);
    assertEquals(20, sources.level(false));
    assertEquals(300, sources.sources().get("garlic").ticksLeft());
  }

  @Test
  @DisplayName("being downed or escaping is weakened at the cap whatever else applies")
  void downedIsAtCap() {
    assertEquals(WeakenedSources.CAP, WeakenedSources.NONE.level(true));
  }

  @Test
  @DisplayName("sources run out")
  void sourcesExpire() {
    WeakenedSources sources = WeakenedSources.NONE.with("holy", 40, 2);
    sources = sources.tick();
    assertEquals(40, sources.level(false));
    sources = sources.tick();
    assertTrue(sources.isEmpty());
    assertEquals(0, sources.level(false));
  }

  @ParameterizedTest(name = "strength {0}, ticks {1}")
  @CsvSource({"0, 100", "-5, 100", "10, 0", "10, -1"})
  @DisplayName("an empty or instant source adds nothing")
  void ignoresEmpty(final int strength, final int ticks) {
    assertTrue(WeakenedSources.NONE.with("x", strength, ticks).isEmpty());
  }

  @ParameterizedTest(name = "held at {0} then {1}")
  @CsvSource({"40, 20", "20, 40", "40, 0"})
  @DisplayName("a held source takes exactly the strength it is given, so it can fade")
  void holdingReplaces(final int first, final int second) {
    final WeakenedSources sources =
        WeakenedSources.NONE
            .with("garlic", 10, 100)
            .holding("starving", first, 2)
            .holding("starving", second, 2);
    assertEquals(second, sources.strength("starving"));
    assertEquals(10 + second, sources.level(false));
  }

  @Test
  @DisplayName("an absent source has no strength")
  void absentStrength() {
    assertEquals(0, WeakenedSources.NONE.strength("starving"));
  }

  @Test
  @DisplayName("sources save and load")
  void codecRoundTrips() {
    final WeakenedSources sources = WeakenedSources.NONE.with("garlic", 25, 40);
    final JsonElement json =
        WeakenedSources.CODEC.encodeStart(JsonOps.INSTANCE, sources).getOrThrow();
    assertEquals(sources, WeakenedSources.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }
}
