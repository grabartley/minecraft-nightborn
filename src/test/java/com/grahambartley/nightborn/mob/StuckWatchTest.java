package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.stage.StageStuck;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StuckWatchTest {

  private static final StageStuck HABITS = new StageStuck(20, 0.5, 2.0, 1.5, 3, 1, 3, 10, 6);
  private static final Vec3d ANCHOR = new Vec3d(0.0, 64.0, 0.0);

  @ParameterizedTest(
      name = "best {0}, stalled {1}, gap {2}, moved {3}, pursuing {4}: best {5}, stalled {6}")
  @CsvSource({
    "NaN, 0, 10.0, 0.0, true, 10.0, 0",
    "10.0, 0, 9.4, 0.0, true, 9.4, 0",
    "10.0, 4, 9.6, 0.0, true, 10.0, 5",
    "10.0, 4, 12.0, 1.9, true, 10.0, 5",
    "10.0, 4, 1.5, 0.0, true, NaN, 0",
    "10.0, 4, 10.0, 0.0, false, NaN, 0",
    "10.0, 4, NaN, 0.0, true, NaN, 0",
    "10.0, 4, 10.0, 2.5, true, 10.0, 0",
    "10.0, 4, 11.0, 3.0, true, 10.0, 0"
  })
  @DisplayName(
      "a mob is stalled while it neither gains on its foe nor travels, and the count clears"
          + " when it gains ground, keeps moving after a fleeing foe, reaches it or stops"
          + " chasing")
  void next(
      final double best,
      final int stalled,
      final double gap,
      final double moved,
      final boolean pursuing,
      final double nextBest,
      final int nextStalled) {
    final StuckWatch next =
        new StuckWatch(best, ANCHOR, stalled, 1)
            .next(gap, ANCHOR.add(moved, 0.0, 0.0), pursuing, HABITS);
    assertEquals(nextBest, next.best());
    assertEquals(nextStalled, next.stalled());
  }

  @ParameterizedTest(name = "progress {0}, moved {1}: reroutes {2}")
  @CsvSource({"true, 0.0, 0", "false, 2.5, 2"})
  @DisplayName("only real ground on the foe clears the tries, while travelling keeps them")
  void reroutesKept(final boolean progress, final double moved, final int reroutes) {
    final StuckWatch next =
        new StuckWatch(10.0, ANCHOR, 4, 2)
            .next(progress ? 8.0 : 10.0, ANCHOR.add(moved, 0.0, 0.0), true, HABITS);
    assertEquals(reroutes, next.reroutes());
  }

  @ParameterizedTest(name = "stalled {0}: stuck {1}")
  @CsvSource({"0, false", "19, false", "20, true", "45, true"})
  @DisplayName("a mob is stuck once it has stalled for its stage's wait")
  void stuck(final int stalled, final boolean stuck) {
    assertEquals(stuck, new StuckWatch(10.0, ANCHOR, stalled, 0).stuck(HABITS));
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
      "a stuck mob first lands, then steps back to find another way, and gives up after its"
          + " tries")
  void step(final int stalled, final int reroutes, final StuckWatch.Step step) {
    assertEquals(step, new StuckWatch(10.0, ANCHOR, stalled, reroutes).step(HABITS));
  }

  @ParameterizedTest(name = "reroutes {0}")
  @CsvSource({"0", "2"})
  @DisplayName("stepping back starts a fresh wait but keeps the closest it ever got")
  void rerouted(final int reroutes) {
    assertEquals(
        new StuckWatch(7.0, ANCHOR, 0, reroutes + 1),
        new StuckWatch(7.0, ANCHOR, 40, reroutes).rerouted());
  }
}
