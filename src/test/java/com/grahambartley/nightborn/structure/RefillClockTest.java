package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RefillClockTest {

  @ParameterizedTest(name = "{1} to {2} days with draw {3} from {0} -> {4}")
  @CsvSource({
    "0, 15, 40, 0, 360000",
    "0, 15, 40, 25, 960000",
    "1000, 15, 40, 10, 601000",
    "0, 7, 7, 0, 168000"
  })
  @DisplayName("a refill time is drawn between the fewest and the most days, in world ticks")
  void drawn(final long now, final int min, final int max, final int draw, final long expected) {
    assertEquals(expected, RefillClock.drawn(now, min, max, bound -> Math.min(draw, bound - 1)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "every rule met, true, true, false, true",
    "never recorded, false, true, false, false",
    "still lived in, true, false, false, false",
    "claimed by a player, true, true, true, false"
  })
  @DisplayName("a household moves in only where one lived, all are gone and no player claims it")
  void mayArrive(
      final String name,
      final boolean known,
      final boolean gone,
      final boolean claimed,
      final boolean arrives) {
    final HouseholdRecord record =
        gone ? HouseholdRecord.NONE : HouseholdRecord.NONE.enlisted(new UUID(0, 1));
    assertEquals(arrives, RefillClock.mayArrive(known, record, claimed));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "no time drawn yet, true, -1, 0, true",
    "time drawn and waiting, true, 360000, 0, false",
    "time drawn at the most days, true, 960000, 0, false",
    "clock turned back past the most days, true, 960000, -1000, true",
    "time already due, true, 1000, 5000, false",
    "still lived in, false, -1, 0, false"
  })
  @DisplayName("a refill time is drawn again only when none is set or the clock was turned back")
  void redraws(
      final String name,
      final boolean gone,
      final long refillAt,
      final long now,
      final boolean redraws) {
    HouseholdRecord record =
        gone ? HouseholdRecord.NONE : HouseholdRecord.NONE.enlisted(new UUID(0, 1));
    if (refillAt >= 0) {
      record = record.refillingAt(refillAt);
    }
    assertEquals(redraws, RefillClock.redraws(record, now, 40));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "empty and unclaimed with no time, true, -1, 0, false, DRAW",
    "empty and unclaimed before its time, true, 360000, 0, false, WAIT",
    "empty and unclaimed at its time, true, 360000, 360000, false, ARRIVE",
    "clock turned back past the most days, true, 960000, -1000, false, DRAW",
    "claimed with a time drawn, true, 360000, 0, true, CLEAR",
    "claimed with no time, true, -1, 0, true, WAIT",
    "claimed and past its time, true, 360000, 900000, true, CLEAR",
    "still lived in, false, -1, 0, false, WAIT"
  })
  @DisplayName("a claimed structure keeps no refill time, so its clock starts when it is let go")
  void step(
      final String name,
      final boolean gone,
      final long refillAt,
      final long now,
      final boolean claimed,
      final RefillClock.Step step) {
    HouseholdRecord record =
        gone ? HouseholdRecord.NONE : HouseholdRecord.NONE.enlisted(new UUID(0, 1));
    if (refillAt >= 0) {
      record = record.refillingAt(refillAt);
    }
    assertEquals(step, RefillClock.step(record, now, 40, claimed));
  }

  @ParameterizedTest(name = "refill at {0}")
  @CsvSource({"-1", "360000"})
  @DisplayName("clearing a refill time leaves the household and its head coffin as they were")
  void unscheduled(final long refillAt) {
    HouseholdRecord record = HouseholdRecord.NONE;
    if (refillAt >= 0) {
      record = record.refillingAt(refillAt);
    }
    final HouseholdRecord cleared = record.unscheduled();
    assertEquals(Optional.empty(), cleared.refillAt());
    assertEquals(record.living(), cleared.living());
    assertEquals(record.headCoffin(), cleared.headCoffin());
  }
}
