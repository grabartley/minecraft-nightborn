package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JournalLogTest {

  static Stream<Arguments> owed() {
    return Stream.of(
        Arguments.of(Set.of(), Optional.empty(), Optional.empty()),
        Arguments.of(Set.of(), Optional.of(Journal.HUNTER), Optional.of(Journal.HUNTER)),
        Arguments.of(Set.of(Journal.HUNTER), Optional.of(Journal.HUNTER), Optional.empty()),
        Arguments.of(
            Set.of(Journal.HUNTER), Optional.of(Journal.VAMPIRE), Optional.of(Journal.VAMPIRE)),
        Arguments.of(
            Set.of(Journal.HUNTER, Journal.VAMPIRE),
            Optional.of(Journal.VAMPIRE),
            Optional.empty()));
  }

  @ParameterizedTest(name = "given {0}, side {1} -> owed {2}")
  @MethodSource("owed")
  @DisplayName("a player who chose a side is owed that side's journal only if never given it")
  void owesOnlyTheFirstTime(
      final Set<Journal> given, final Optional<Journal> side, final Optional<Journal> expected) {
    assertEquals(expected, new JournalLog(given).owed(side));
  }

  static Stream<Arguments> logs() {
    return Stream.of(
        Arguments.of(Set.of()),
        Arguments.of(Set.of(Journal.HUNTER)),
        Arguments.of(Set.of(Journal.HUNTER, Journal.VAMPIRE)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("logs")
  @DisplayName("recording a journal keeps the earlier ones, and the log saves and loads whole")
  void recordsAndSaves(final Set<Journal> given) {
    final JournalLog log = new JournalLog(given).with(Journal.VAMPIRE);
    assertEquals(true, log.given().contains(Journal.VAMPIRE));
    assertEquals(true, log.given().containsAll(given));
    assertEquals(
        log,
        JournalLog.CODEC
            .parse(
                JsonOps.INSTANCE, JournalLog.CODEC.encodeStart(JsonOps.INSTANCE, log).getOrThrow())
            .getOrThrow());
  }
}
