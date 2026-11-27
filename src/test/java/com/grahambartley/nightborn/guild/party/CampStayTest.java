package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

  @ParameterizedTest(name = "{0} nights from day {1} at tick {2} survives a save and load")
  @CsvSource({"1, 0, 12000", "3, 40, 972000", "2, 7, -1"})
  void survivesASaveAndLoad(final int nights, final long campDay, final long pitchedAt) {
    final CampStay stay = new CampStay(nights, campDay, pitchedAt);
    assertEquals(
        stay,
        CampStay.CODEC
            .parse(NbtOps.INSTANCE, CampStay.CODEC.encodeStart(NbtOps.INSTANCE, stay).getOrThrow())
            .getOrThrow());
  }
}
