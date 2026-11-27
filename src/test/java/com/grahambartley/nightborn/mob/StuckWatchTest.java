package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StuckWatchTest {

  @ParameterizedTest(name = "best {0}, stalled {1}, at {2}, pursuing {3}: best {4}, stalled {5}")
  @CsvSource({
    "NaN, 0, 10.0, true, 10.0, 0",
    "10.0, 0, 9.4, true, 9.4, 0",
    "10.0, 4, 9.6, true, 10.0, 5",
    "10.0, 4, 12.0, true, 10.0, 5",
    "10.0, 4, 2.5, true, NaN, 0",
    "10.0, 4, 10.0, false, NaN, 0",
    "10.0, 4, NaN, true, NaN, 0"
  })
  @DisplayName(
      "a mob is stalled while it gets no closer to its quarry, and the count clears when it"
          + " gains ground, reaches it or stops chasing")
  void next(
      final double best,
      final int stalled,
      final double distance,
      final boolean pursuing,
      final double nextBest,
      final int nextStalled) {
    final StuckWatch next = new StuckWatch(best, stalled, 1).next(distance, pursuing);
    assertEquals(nextBest, next.best());
    assertEquals(nextStalled, next.stalled());
  }

  @ParameterizedTest(name = "stalled {0}, patience {1}: stuck {2}")
  @CsvSource({"0, 20, false", "19, 20, false", "20, 20, true", "45, 20, true"})
  @DisplayName("a mob is stuck once it has stalled for its patience")
  void stuck(final int stalled, final int patience, final boolean stuck) {
    assertEquals(stuck, new StuckWatch(10.0, stalled, 0).stuck(patience));
  }

  @ParameterizedTest(name = "stalled {0}, reroutes {1}: {2}")
  @CsvSource({
    "0, 0, NONE",
    "19, 0, NONE",
    "20, 0, LAND",
    "21, 0, NONE",
    "40, 0, STEP_BACK",
    "40, 1, STEP_BACK",
    "40, 2, GIVE_UP",
    "41, 0, NONE"
  })
  @DisplayName(
      "a stuck mob first lands, then steps back to find another way, and gives up after a few"
          + " tries")
  void step(final int stalled, final int reroutes, final StuckWatch.Step step) {
    assertEquals(step, new StuckWatch(10.0, stalled, reroutes).step(20));
  }

  @ParameterizedTest(name = "reroutes {0}")
  @CsvSource({"0", "2"})
  @DisplayName("stepping back starts a fresh wait but keeps the closest it ever got")
  void rerouted(final int reroutes) {
    final StuckWatch next = new StuckWatch(7.0, 40, reroutes).rerouted();
    assertEquals(new StuckWatch(7.0, 0, reroutes + 1), next);
  }
}
