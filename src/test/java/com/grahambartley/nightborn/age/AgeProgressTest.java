package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeProgressTest {

  @ParameterizedTest(name = "rank {0}, nights {1}, blood {2}, deeds {3}")
  @CsvSource({"0, 0, 0, 0", "1, 10, 100, 2", "3, 100, 1500, 14", "15, 1000000, 1000000, 65535"})
  @DisplayName("progress is saved and synced unchanged")
  void roundTrips(final int rank, final int nights, final int blood, final int deeds) {
    final AgeProgress progress = new AgeProgress(rank, nights, blood, deeds);
    assertEquals(
        progress,
        AgeProgress.CODEC
            .parse(
                JsonOps.INSTANCE,
                AgeProgress.CODEC.encodeStart(JsonOps.INSTANCE, progress).getOrThrow())
            .getOrThrow());
    final ByteBuf buf = Unpooled.buffer();
    AgeProgress.PACKET_CODEC.encode(buf, progress);
    assertEquals(progress, AgeProgress.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "rank {0}, nights {1}, blood {2} -> {3}, {4}, {5}")
  @CsvSource({"-1, -5, -9, 0, 0, 0", "99, 2000000, 2000000, 15, 1000000, 1000000"})
  @DisplayName("values outside their range are clamped")
  void clamps(
      final int rank,
      final int nights,
      final int blood,
      final int keptRank,
      final int keptNights,
      final int keptBlood) {
    assertEquals(
        new AgeProgress(keptRank, keptNights, keptBlood, 0),
        new AgeProgress(rank, nights, blood, 0));
  }

  @ParameterizedTest(name = "{0} + {1} nights, {2} + {3} blood")
  @CsvSource({"0, 1, 0, 5", "9, 1, 95, 5", "999999, 5, 999999, 5", "4, -3, 4, -3"})
  @DisplayName("nights and blood only ever add up, and stop at the cap")
  void addsUp(final int nights, final int more, final int blood, final int drunk) {
    final AgeProgress progress = new AgeProgress(0, nights, blood, 0).lived(more).drank(drunk);
    assertEquals(Math.min(AgeProgress.MOST, nights + Math.max(0, more)), progress.nights());
    assertEquals(Math.min(AgeProgress.MOST, blood + Math.max(0, drunk)), progress.blood());
  }

  @ParameterizedTest(name = "deed {0}")
  @CsvSource({"0", "1", "2", "3", "15"})
  @DisplayName("a deed done is remembered and leaves the others alone")
  void remembersDeeds(final int rank) {
    final AgeProgress progress = AgeProgress.EMPTY.withDeed(rank);
    for (int other = 0; other <= AgeProgress.TOP_RANK; other++) {
      assertEquals(other == rank, progress.deedDone(other));
    }
    assertEquals(progress, progress.withDeed(rank));
  }

  @ParameterizedTest(name = "deed {0}")
  @CsvSource({"-1", "16", "40"})
  @DisplayName("a deed outside the ladder is never done")
  void ignoresDeedsOffTheLadder(final int rank) {
    assertEquals(AgeProgress.EMPTY, AgeProgress.EMPTY.withDeed(rank));
    assertFalse(AgeProgress.EMPTY.withDeed(rank).deedDone(rank));
  }

  @Test
  @DisplayName("a changed rank keeps nights, blood and deeds")
  void rankKeepsTheRest() {
    assertEquals(new AgeProgress(2, 7, 9, 6), new AgeProgress(0, 7, 9, 6).withRank(2));
    assertTrue(new AgeProgress(0, 7, 9, 6).withRank(2).deedDone(1));
  }
}
