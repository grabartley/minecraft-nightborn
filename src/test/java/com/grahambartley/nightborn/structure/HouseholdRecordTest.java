package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HouseholdRecordTest {

  private static final UUID FIRST = new UUID(0, 1);
  private static final UUID SECOND = new UUID(0, 2);
  private static final GlobalPos COFFIN = GlobalPos.create(World.OVERWORLD, new BlockPos(5, 60, 5));

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(HouseholdRecord.NONE, Set.of(), true),
        Arguments.of(HouseholdRecord.NONE.enlisted(FIRST), Set.of(FIRST), false),
        Arguments.of(HouseholdRecord.NONE.enlisted(FIRST).enlisted(FIRST), Set.of(FIRST), false),
        Arguments.of(
            HouseholdRecord.NONE.enlisted(FIRST).enlisted(SECOND).forgot(FIRST),
            Set.of(SECOND),
            false),
        Arguments.of(HouseholdRecord.NONE.enlisted(FIRST).forgot(FIRST), Set.of(), true),
        Arguments.of(HouseholdRecord.NONE.forgot(FIRST), Set.of(), true));
  }

  @ParameterizedTest(name = "{index}: {1}, gone {2}")
  @MethodSource("changes")
  @DisplayName("a household is gone once the last of its living is forgotten")
  void living(final HouseholdRecord record, final Set<UUID> living, final boolean gone) {
    assertEquals(living, record.living());
    assertEquals(gone, record.gone());
  }

  static Stream<Arguments> records() {
    return Stream.of(
        Arguments.of(HouseholdRecord.NONE),
        Arguments.of(HouseholdRecord.NONE.enlisted(FIRST).headSleepsAt(COFFIN)),
        Arguments.of(
            new HouseholdRecord(Set.of(FIRST, SECOND), Optional.empty(), Optional.of(48000L))));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("records")
  @DisplayName("a household's living and its head's coffin survive a save and load")
  void roundTrip(final HouseholdRecord record) {
    assertEquals(
        record,
        HouseholdRecord.CODEC
            .parse(
                JsonOps.INSTANCE,
                HouseholdRecord.CODEC.encodeStart(JsonOps.INSTANCE, record).getOrThrow())
            .getOrThrow());
  }

  static Stream<Arguments> heads() {
    final GlobalPos other = GlobalPos.create(World.OVERWORLD, new BlockPos(6, 60, 5));
    return Stream.of(
        Arguments.of(HouseholdRecord.NONE.headSleepsAt(COFFIN), COFFIN, true),
        Arguments.of(HouseholdRecord.NONE.headSleepsAt(COFFIN), other, false),
        Arguments.of(HouseholdRecord.NONE, COFFIN, false));
  }

  @ParameterizedTest(name = "{index}: {2}")
  @MethodSource("heads")
  @DisplayName("only the coffin its head slept in is the head's coffin")
  void headSleepsIn(final HouseholdRecord record, final GlobalPos coffin, final boolean head) {
    assertEquals(head, record.headSleepsIn(coffin));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("records")
  @DisplayName("enlisting a vampire the household already counts changes nothing")
  void enlistsOnce(final HouseholdRecord record) {
    final HouseholdRecord once = record.enlisted(FIRST);
    org.junit.jupiter.api.Assertions.assertSame(once, once.enlisted(FIRST));
  }

  static Stream<Arguments> refills() {
    final HouseholdRecord cleared = HouseholdRecord.NONE.enlisted(FIRST).forgot(FIRST);
    return Stream.of(
        Arguments.of("an empty household with no time yet", cleared, 100L, true, false),
        Arguments.of(
            "an empty household before its time", cleared.refillingAt(200L), 100L, false, false),
        Arguments.of(
            "an empty household at its time", cleared.refillingAt(200L), 200L, false, true),
        Arguments.of(
            "an empty household past its time", cleared.refillingAt(200L), 900L, false, true),
        Arguments.of(
            "a living household is never timed",
            HouseholdRecord.NONE.enlisted(FIRST).refillingAt(200L),
            900L,
            false,
            false),
        Arguments.of(
            "a member arriving forgets the time",
            cleared.refillingAt(200L).enlisted(SECOND),
            900L,
            false,
            false),
        Arguments.of(
            "a household that moved in is no longer waiting",
            cleared.refillingAt(200L).refilledBy(Set.of(FIRST, SECOND)),
            900L,
            false,
            false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("refills")
  @DisplayName("an empty household waits for a time drawn once, then is due from that time on")
  void refill(
      final String name,
      final HouseholdRecord record,
      final long now,
      final boolean awaitsTime,
      final boolean due) {
    assertEquals(awaitsTime, record.awaitsRefillTime());
    assertEquals(due, record.refillDue(now));
  }

  @ParameterizedTest(name = "{0} arrivals")
  @MethodSource("arrivals")
  @DisplayName("a new household replaces the living and keeps the head's coffin")
  void refilledBy(final Set<UUID> arrivals) {
    final HouseholdRecord refilled =
        HouseholdRecord.NONE.headSleepsAt(COFFIN).refillingAt(5L).refilledBy(arrivals);
    assertEquals(arrivals, refilled.living());
    assertEquals(Optional.of(COFFIN), refilled.headCoffin());
    assertEquals(Optional.empty(), refilled.refillAt());
  }

  static Stream<Set<UUID>> arrivals() {
    return Stream.of(Set.of(), Set.of(FIRST), Set.of(FIRST, SECOND));
  }
}
