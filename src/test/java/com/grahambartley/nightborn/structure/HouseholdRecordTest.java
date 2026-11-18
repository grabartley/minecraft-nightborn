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
        Arguments.of(new HouseholdRecord(Set.of(FIRST, SECOND), Optional.empty())));
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
}
