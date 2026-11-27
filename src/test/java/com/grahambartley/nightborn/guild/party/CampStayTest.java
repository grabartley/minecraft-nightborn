package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CampStayTest {

  @ParameterizedTest(
      name = "{0} nights from day {1} at tick {2}: time of day {3}, game time {4}, over {5}")
  @CsvSource({
    "1, 0, 12000, 18000, 18000, false",
    "1, 0, 12000, 24000, 24000, true",
    "2, 0, 12000, 24500, 24500, false",
    "2, 0, 12000, 48500, 48500, true",
    "3, 0, 12000, 71999, 71999, false",
    "3, 0, 12000, 72000, 72000, true",
    "3, 5, 132000, 1000, 140000, true",
    "3, 5, 132000, 119999, 140000, true",
    "1, 5, 132000, 132500, 155999, false",
    "1, 5, 132000, 132500, 156000, true",
    "3, 5, 132000, 132500, 203999, false",
    "3, 5, 132000, 132500, 204000, true",
    "1, 5, -1, 132500, 140000, true",
    "1, 5, 140001, 132500, 140000, true",
    "3, 5, 132000, 24000000, 140000, true"
  })
  void endsTheStayWhicheverClockRunsOutFirst(
      final int nights,
      final long campDay,
      final long pitchedAt,
      final long timeOfDay,
      final long now,
      final boolean over) {
    assertEquals(over, new CampStay(nights, campDay, pitchedAt).over(timeOfDay, now));
  }

  @Test
  void noStayIsAlwaysOver() {
    assertEquals(true, CampStay.NONE.over(0L, 0L));
  }

  @ParameterizedTest(name = "guard camp released at {0}: time of day {1}, game time {2}, over {3}")
  @CsvSource({
    "-1, 6000, 9000000, false",
    "-1, 18000, 9000000, false",
    "100000, 30000, 100020, true",
    "100000, 37000, 100020, false",
    "100000, 46999, 109999, false",
    "100000, 47000, 110000, true",
    "100000, 37000, 110000, true",
    "100000, 37000, 99000, true"
  })
  void aGuardCampStandsUntilItsVillageLetsItGoAndThenLeavesByDawn(
      final long releasedAt, final long timeOfDay, final long now, final boolean over) {
    final CampStay stay =
        new CampStay(0, 1L, 30_000L, Optional.of(new BlockPos(40, 64, -12)), releasedAt);
    assertEquals(over, stay.over(timeOfDay, now));
  }

  @ParameterizedTest(name = "guard camp {0}, already released {1}: released at 500 becomes {2}")
  @CsvSource({"true, false, 500", "true, true, 200", "false, false, -1"})
  void onlyAGuardCampIsReleasedAndOnlyOnce(
      final boolean guard, final boolean already, final long releasedAt) {
    final CampStay pitched =
        guard
            ? CampStay.guarding(new BlockPos(40, 64, -12), 1L, 30_000L)
            : new CampStay(2, 1L, 30_000L);
    final CampStay stay = already ? pitched.released(200L) : pitched;
    assertEquals(releasedAt, stay.released(500L).releasedAt());
  }

  @ParameterizedTest(name = "{0} nights from day {1} at tick {2} survives a save and load")
  @CsvSource({"1, 0, 12000", "3, 40, 972000", "2, 7, -1"})
  void survivesASaveAndLoad(final int nights, final long campDay, final long pitchedAt) {
    final CampStay stay = new CampStay(nights, campDay, pitchedAt);
    assertEquals(stay, reloaded(stay));
  }

  @ParameterizedTest(name = "a guard camp released at {0} survives a save and load")
  @ValueSource(longs = {-1L, 0L, 410_000L})
  void aGuardCampSurvivesASaveAndLoad(final long releasedAt) {
    final CampStay stay =
        new CampStay(0, 12L, 300_000L, Optional.of(new BlockPos(-3, 70, 88)), releasedAt);
    assertEquals(stay, reloaded(stay));
  }

  private static CampStay reloaded(final CampStay stay) {
    return CampStay.CODEC
        .parse(NbtOps.INSTANCE, CampStay.CODEC.encodeStart(NbtOps.INSTANCE, stay).getOrThrow())
        .getOrThrow();
  }
}
