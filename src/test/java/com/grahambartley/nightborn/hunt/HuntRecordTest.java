package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HuntRecordTest {

  static final UUID MAN = UUID.fromString("00000000-0000-0000-0000-000000000001");

  static Stream<Arguments> records() {
    return Stream.of(
        Arguments.of(HuntRecord.EMPTY),
        Arguments.of(HuntRecord.EMPTY.withNotoriety(12.5)),
        Arguments.of(HuntRecord.EMPTY.withNotoriety(31).warning(2)),
        Arguments.of(HuntRecord.EMPTY.withNotoriety(31).due()),
        Arguments.of(HuntRecord.EMPTY.withNotoriety(31).visitedBy(MAN)),
        Arguments.of(HuntRecord.EMPTY.withNotoriety(31).ended(4, 10)),
        Arguments.of(HuntRecord.EMPTY.withNotoriety(31).visitedBy(MAN).named("Ezra")));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"Ezra the Grand Inquisitor", "Silas the Grand Inquisitor"})
  @DisplayName("the man of a hunt is kept until the hunt ends, then forgotten")
  void keepsTheMan(final String name) {
    final HuntRecord named = HuntRecord.EMPTY.withNotoriety(31).visitedBy(MAN).named(name);
    assertEquals(Optional.of(name), named.returning().man());
    assertEquals(Optional.of(name), named.returning().due().visitedBy(MAN).man());
    assertEquals(Optional.empty(), named.ended(4, 10).man());
  }

  @ParameterizedTest(name = "notoriety {0}")
  @CsvSource({"31", "120"})
  @DisplayName("a man who left an empty arena is due again at the next dusk, with no warning")
  void returnsAtDusk(final double notoriety) {
    final HuntRecord returning =
        HuntRecord.EMPTY.withNotoriety(notoriety).visitedBy(MAN).returning();
    assertEquals(HuntRecord.Phase.WARNING, returning.phase());
    assertEquals(0, returning.warningsLeft());
    final HuntRules.Dusk dusk = HuntRules.dusk(returning);
    assertEquals(HuntRecord.Phase.DUE, dusk.record().phase());
    assertEquals(0, dusk.warned());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("records")
  @DisplayName("a hunt survives a save and a load unchanged")
  void roundTrips(final HuntRecord record) {
    final var saved = HuntRecord.CODEC.encodeStart(JsonOps.INSTANCE, record).getOrThrow();
    assertEquals(record, HuntRecord.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow());
  }

  @ParameterizedTest(name = "{0} kept as {1}")
  @CsvSource({"-4, 0", "0, 0", "7.25, 7.25"})
  @DisplayName("notoriety never goes below nothing")
  void neverNegative(final double notoriety, final double kept) {
    assertEquals(kept, HuntRecord.EMPTY.withNotoriety(notoriety).notoriety());
  }

  @Test
  @DisplayName("only a visit remembers who came")
  void visitorOnlyOnVisit() {
    assertEquals(Optional.of(MAN), HuntRecord.EMPTY.visitedBy(MAN).visitor());
    assertEquals(Optional.empty(), HuntRecord.EMPTY.visitedBy(MAN).due().visitor());
    assertEquals(
        Optional.empty(),
        new HuntRecord(
                1, HuntRecord.Phase.DUE, 0, Optional.of(MAN), HuntRecord.NEVER, Optional.empty())
            .visitor());
  }

  @ParameterizedTest(
      name = "ended on night {0} with {1} quiet nights, on night {2}: quiet {3}, {4} left")
  @CsvSource({
    "4, 10, 4, true, 10",
    "4, 10, 5, true, 9",
    "4, 10, 14, true, 0",
    "4, 10, 15, false, 0",
    "4, 0, 4, false, 0",
    "4, 1, 5, true, 0",
    "4, 1, 6, false, 0"
  })
  @DisplayName("quiet nights start when a hunt ends and run out after the set number")
  void quietNights(
      final long ended,
      final int quietNights,
      final long night,
      final boolean quiet,
      final long left) {
    final HuntRecord record = HuntRecord.EMPTY.withNotoriety(40).due().ended(ended, quietNights);
    assertEquals(HuntRecord.Phase.NONE, record.phase());
    assertEquals(quiet, record.quiet(night));
    assertEquals(left, record.quietNightsLeft(night));
  }

  @Test
  @DisplayName("a fresh record has no quiet nights")
  void freshIsNotQuiet() {
    assertFalse(HuntRecord.EMPTY.quiet(Long.MIN_VALUE + 1));
    assertEquals(0L, HuntRecord.EMPTY.quietNightsLeft(0));
  }

  @ParameterizedTest(name = "notoriety {0}, hunted {1}, on night {2}: idle {3}")
  @CsvSource({
    "0, false, 20, true",
    "0.5, false, 20, false",
    "0, true, 20, false",
    "0, false, 4, false"
  })
  @DisplayName("a record with nothing left in it can be dropped")
  void idle(final double notoriety, final boolean hunted, final long night, final boolean idle) {
    HuntRecord record = HuntRecord.EMPTY.withNotoriety(notoriety).ended(3, 10);
    record = hunted ? record.warning(1) : record;
    assertEquals(idle, record.idle(night));
    assertTrue(HuntRecord.EMPTY.idle(0));
  }

  @ParameterizedTest(name = "named {0}")
  @CsvSource({"Ezra the Grand Inquisitor", "''"})
  @DisplayName("a visit remembers the man and, when he has one, his name")
  void visitedByName(final String name) {
    final Optional<String> given = name.isEmpty() ? Optional.empty() : Optional.of(name);
    final HuntRecord visited = HuntRecord.EMPTY.withNotoriety(31).visitedBy(MAN, given);
    assertEquals(Optional.of(MAN), visited.visitor());
    assertEquals(given, visited.man());
  }
}
