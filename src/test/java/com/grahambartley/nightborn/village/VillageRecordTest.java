package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageRecordTest {

  private static final UUID FACE = UUID.randomUUID();

  private static VillageRecord at(final int suspicion) {
    return VillageRecord.fresh(0L).raised(suspicion, -1L, Optional.of(FACE));
  }

  @ParameterizedTest(name = "{0} + {1} = {2}")
  @CsvSource({"0, 25, 25", "60, 25, 85", "90, 25, 100", "0, 0, 0"})
  @DisplayName("suspicion rises by what was seen and never passes 100")
  void rises(final int start, final int amount, final int expected) {
    assertEquals(expected, at(start).raised(amount, 0L, Optional.empty()).suspicion());
  }

  @ParameterizedTest(name = "seen {0}: known face {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("only a vampire someone saw becomes a known face")
  void seenBecomesKnown(final boolean seen, final boolean known) {
    final UUID vampire = UUID.randomUUID();
    final VillageRecord record =
        VillageRecord.fresh(0L).raised(25, 0L, seen ? Optional.of(vampire) : Optional.empty());
    assertEquals(known, record.knows(vampire));
  }

  @ParameterizedTest(name = "{0} from night {1} to {2}, incident on {3}, {4} neighbours taken: {5}")
  @CsvSource({
    "25, 0, 1, -100, 0, 23",
    "25, 0, 3, -100, 0, 19",
    "25, 0, 1, 0, 0, 25",
    "25, 0, 3, 0, 0, 21",
    "25, 0, 0, -100, 0, 25",
    "25, 0, 2, -100, 1, 27",
    "25, 0, 1, -100, 3, 28",
    "0, 0, 1, -100, 1, 1",
    "0, 0, 3, -100, 2, 6",
    "3, 0, 5, -100, 0, 0"
  })
  @DisplayName(
      "each quiet night takes off 2, and a night with a taken neighbour is not quiet: each adds 1")
  void settlesByTheClock(
      final int start,
      final long from,
      final long to,
      final long incident,
      final int taken,
      final int expected) {
    final VillageRecord record =
        new VillageRecord(
            start,
            from,
            incident,
            VillageRecord.NEVER,
            VillageRecord.NEVER,
            0,
            VillageRecord.NEVER,
            VillageRecord.NEVER,
            List.of(FACE),
            List.of(),
            List.of());
    final VillageRecord settled = record.settled(to, taken, 1, 2);
    assertEquals(expected, settled.suspicion());
    assertEquals(Math.max(from, to), settled.settledNight());
  }

  @ParameterizedTest(name = "suspicion falling to {0} keeps the face: {1}")
  @CsvSource({"0, false", "1, true"})
  @DisplayName("faces are forgotten once the village is calm again")
  void forgetsFacesAtZero(final int left, final boolean known) {
    final VillageRecord record = at(10).charmed(3L, 10 - left);
    assertEquals(left, record.suspicion());
    assertEquals(known, record.knows(FACE));
  }

  @ParameterizedTest(name = "charmed on nights {0} and {1}: {2}")
  @CsvSource({"4, 4, 45", "4, 5, 40"})
  @DisplayName("a charmed villager takes off 5 more, once a night")
  void charmOnceANight(final long first, final long second, final int expected) {
    assertEquals(expected, at(50).charmed(first, 5).charmed(second, 5).suspicion());
  }

  @ParameterizedTest(name = "{0} gifts on one night leave {1}")
  @CsvSource({"1, 48", "3, 44", "4, 44", "10, 44"})
  @DisplayName("gifts and good trades take off 2 each, up to 3 a night")
  void giftsUpToThreeANight(final int gifts, final int expected) {
    VillageRecord record = at(50);
    for (int i = 0; i < gifts; i++) {
      record = record.gifted(7L, 2, 3);
    }
    assertEquals(expected, record.suspicion());
  }

  @ParameterizedTest(name = "three gifts on night 7 then {0} on night 8 leave {1}")
  @CsvSource({"1, 42", "3, 38"})
  @DisplayName("the gift allowance starts again each night")
  void giftsResetEachNight(final int more, final int expected) {
    VillageRecord record = at(50).gifted(7L, 2, 3).gifted(7L, 2, 3).gifted(7L, 2, 3);
    for (int i = 0; i < more; i++) {
      record = record.gifted(8L, 2, 3);
    }
    assertEquals(expected, record.suspicion());
  }

  @ParameterizedTest(name = "suspicion {0}, {1} taken, {2} wards: idle {3}")
  @CsvSource({"0, 0, 0, true", "5, 0, 0, false", "0, 1, 0, false", "0, 0, 1, false"})
  @DisplayName("a village is forgotten only when calm, with nobody taken and no wards up")
  void idle(final int suspicion, final int taken, final int wards, final boolean idle) {
    VillageRecord record = VillageRecord.fresh(0L).raised(suspicion, 0L, Optional.empty());
    for (int i = 0; i < taken; i++) {
      record = record.withTaken(new TakenNeighbour(UUID.randomUUID(), UUID.randomUUID()));
    }
    if (wards > 0) {
      record = record.withWards(List.of(new BlockPos(1, 2, 3)));
    }
    assertEquals(idle, record.idle());
  }

  @ParameterizedTest(name = "kept {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a taken neighbour is counted only while still kept, and only once")
  void keepsTakenNeighbours(final boolean kept) {
    final TakenNeighbour taken = new TakenNeighbour(UUID.randomUUID(), UUID.randomUUID());
    final VillageRecord record =
        VillageRecord.fresh(0L).withTaken(taken).withTaken(taken).keepingTaken(any -> kept);
    assertEquals(kept ? List.of(taken) : List.of(), record.taken());
  }

  @ParameterizedTest(name = "sent on night {0}")
  @CsvSource({"3", "9"})
  void remembersTheNightTheGuildWasSent(final long night) {
    assertEquals(night, at(80).sent(night).sentNight());
  }

  @ParameterizedTest(name = "suspicion {0} survives a save and load")
  @CsvSource({"0", "40", "100"})
  void survivesASaveAndLoad(final int suspicion) {
    final VillageRecord record =
        at(suspicion)
            .withTaken(new TakenNeighbour(UUID.randomUUID(), UUID.randomUUID()))
            .withWards(List.of(new BlockPos(4, 70, -9)))
            .sent(2L);
    final VillageRecord loaded =
        VillageRecord.CODEC
            .parse(
                NbtOps.INSTANCE,
                VillageRecord.CODEC.encodeStart(NbtOps.INSTANCE, record).getOrThrow())
            .getOrThrow();
    assertEquals(record, loaded);
    assertTrue(loaded.wards().contains(new BlockPos(4, 70, -9)));
    assertFalse(loaded.taken().isEmpty());
  }

  @ParameterizedTest(name = "hung on night {0}")
  @CsvSource({"2", "6"})
  void remembersTheNightTheWardsWentUp(final long night) {
    final VillageRecord hung = at(60).hung(night, List.of(new BlockPos(1, 64, 1)));
    assertEquals(night, hung.wardNight());
    assertEquals(List.of(new BlockPos(1, 64, 1)), hung.wards());
  }
}
