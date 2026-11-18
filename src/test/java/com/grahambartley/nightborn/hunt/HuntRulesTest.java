package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.HuntSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HuntRulesTest {

  static final HuntSettings SETTINGS = new HuntSettings(15, 30, 1, 10, 3, 18000, 200);

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({"0, NONE", "15, NONE", "15.25, RAID", "30, RAID", "30.5, HUNT", "200, HUNT"})
  @DisplayName("past the first threshold the guild raids, past the second it hunts")
  void levels(final double notoriety, final HuntRules.Level level) {
    assertEquals(level, HuntRules.level(notoriety, SETTINGS));
  }

  @ParameterizedTest(name = "{0} + {1}, quiet {2} -> {3}")
  @CsvSource({
    "0, 1, false, 1",
    "14, 6, false, 20",
    "29, 5, false, 34",
    "14, 6, true, 15",
    "15, 6, true, 15",
    "20, 20, true, 30",
    "31, 5, true, 36",
    "10, -3, false, 10"
  })
  @DisplayName("in quiet nights notoriety still rises, but never past a threshold it was under")
  void gains(final double before, final double amount, final boolean quiet, final double after) {
    assertEquals(after, HuntRules.gain(before, amount, quiet, SETTINGS));
  }

  @ParameterizedTest(name = "{0} at {1} on night {3} -> {4} with {5} warnings")
  @CsvSource({
    "31, NONE, 0, 5, WARNING, 3",
    "30, NONE, 0, 5, NONE, 0",
    "31, NONE, 9, 5, NONE, 0",
    "31, NONE, 9, 20, WARNING, 3",
    "29, WARNING, 0, 5, NONE, 0",
    "29, DUE, 0, 5, NONE, 0",
    "31, WARNING, 0, 5, WARNING, 2",
    "10, VISIT, 0, 5, VISIT, 0"
  })
  @DisplayName(
      "a hunt starts past the second threshold outside quiet nights, and fading calls it off")
  void settles(
      final double notoriety,
      final HuntRecord.Phase phase,
      final int endedOn,
      final long night,
      final HuntRecord.Phase settled,
      final int warnings) {
    HuntRecord record = HuntRecord.EMPTY.withNotoriety(notoriety);
    if (endedOn > 0) {
      record = record.ended(endedOn, SETTINGS.quietNights());
    }
    record =
        switch (phase) {
          case NONE -> record;
          case WARNING -> record.warning(2);
          case DUE -> record.due();
          case VISIT -> record.visitedBy(HuntRecordTest.MAN);
        };
    final HuntRecord after = HuntRules.settle(record, night, SETTINGS);
    assertEquals(settled, after.phase());
    assertEquals(warnings, after.warningsLeft());
  }

  @ParameterizedTest(name = "a hunt called off on night {0} is quiet on night {1}: {2}")
  @CsvSource({"5, 5, true", "5, 15, true", "5, 16, false"})
  @DisplayName("a hunt called off by fading still brings quiet nights")
  void calledOffIsQuiet(final long night, final long later, final boolean quiet) {
    final HuntRecord off =
        HuntRules.settle(HuntRecord.EMPTY.withNotoriety(20).due(), night, SETTINGS);
    assertEquals(quiet, off.quiet(later));
  }

  @ParameterizedTest(name = "{0} fades to {1}")
  @CsvSource({"10, 9", "1, 0", "0.5, 0", "0, 0"})
  @DisplayName("each dawn takes the set fade off, never below nothing")
  void fades(final double before, final double after) {
    assertEquals(
        after, HuntRules.faded(HuntRecord.EMPTY.withNotoriety(before), SETTINGS).notoriety());
  }

  @ParameterizedTest(name = "dusk with {0} warnings left -> {1} with {2} left, warned at {3}")
  @CsvSource({"3, WARNING, 2, 3", "2, WARNING, 1, 2", "1, WARNING, 0, 1", "0, DUE, 0, 0"})
  @DisplayName("each dusk warns once, and the dusk after the last warning he is due")
  void dusks(final int left, final HuntRecord.Phase phase, final int nowLeft, final int warned) {
    final HuntRules.Dusk dusk = HuntRules.dusk(HuntRecord.EMPTY.withNotoriety(31).warning(left));
    assertEquals(phase, dusk.record().phase());
    assertEquals(nowLeft, dusk.record().warningsLeft());
    assertEquals(warned, dusk.warned());
  }

  @ParameterizedTest(name = "{0} at dusk stays {0}")
  @CsvSource({"NONE", "DUE"})
  @DisplayName("dusk changes nothing outside the warning nights")
  void duskOutsideWarnings(final HuntRecord.Phase phase) {
    final HuntRecord record =
        phase == HuntRecord.Phase.DUE ? HuntRecord.EMPTY.due() : HuntRecord.EMPTY;
    assertEquals(new HuntRules.Dusk(record, 0), HuntRules.dusk(record));
  }

  @ParameterizedTest(name = "time {0}: window {1}")
  @CsvSource({
    "12999, false",
    "13000, true",
    "17999, true",
    "18000, false",
    "22000, false",
    "1000, false",
    "37000, true"
  })
  @DisplayName("he comes only early in the night, from dusk until the set hour")
  void arrivalWindow(final long time, final boolean open) {
    assertEquals(open, HuntRules.inArrivalWindow(time, SETTINGS));
  }

  @ParameterizedTest(name = "overworld {0}, open sky {1}, window {2}: arrives {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false"
  })
  @DisplayName("he never comes underground, under a roof, outside the overworld or late")
  void arrives(
      final boolean overworld, final boolean sky, final boolean window, final boolean arrives) {
    assertEquals(arrives, HuntRules.canArrive(overworld, sky, window));
  }

  @ParameterizedTest(name = "{0} to {1} crosses {2}: {3}")
  @CsvSource({
    "12999, 13000, 13000, true",
    "13000, 13001, 13000, false",
    "22999, 23000, 23000, true",
    "23000, 23000, 23000, false",
    "23500, 1000, 23000, false",
    "36999, 37000, 13000, true",
    "1000, 30000, 23000, true",
    "1000, 90000, 13000, true",
    "14000, 13500, 13000, false"
  })
  @DisplayName("a dawn or dusk counts once when the clock passes it")
  void crosses(final long from, final long to, final long boundary, final boolean crossed) {
    assertEquals(crossed, HuntRules.crossed(from, to, boundary));
  }

  @ParameterizedTest(name = "{0} of {1} nights left: volume {2}")
  @CsvSource({"3, 3, 0.3", "2, 3, 0.65", "1, 3, 1.0", "1, 1, 1.0", "5, 3, 0.3"})
  @DisplayName("each night the warning bell sounds nearer")
  void tollsNearer(final int left, final int nights, final float volume) {
    assertEquals(volume, HuntRules.tollVolume(left, nights), 0.001F);
  }

  @ParameterizedTest(name = "{0} left: {1}")
  @CsvSource({"5, far", "3, far", "2, nearer", "1, near"})
  @DisplayName("the warning line says how near the guild is")
  void lines(final int left, final String line) {
    assertEquals(line, HuntRules.warningLine(left));
  }
}
