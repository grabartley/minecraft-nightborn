package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.hunt.HuntRecord;
import com.grahambartley.nightborn.hunt.HuntRules;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidRulesTest {

  @ParameterizedTest(name = "{0}, known face {1}: wanted {2}")
  @CsvSource({
    "NONE, false, false",
    "NONE, true, true",
    "RAID, false, true",
    "RAID, true, true",
    "HUNT, false, false",
    "HUNT, true, true"
  })
  void wantsARaidPastTheFirstThresholdOrForAKnownFace(
      final HuntRules.Level level, final boolean knownFace, final boolean wanted) {
    assertEquals(wanted, RaidRules.wanted(level, knownFace));
  }

  @ParameterizedTest(name = "wanted {0}, quiet {1}, {2}: sends {3}")
  @CsvSource({
    "true, false, NONE, true",
    "true, true, NONE, false",
    "false, false, NONE, false",
    "true, false, WARNING, false",
    "true, false, DUE, false",
    "true, false, VISIT, false"
  })
  void sendsOnlyOutsideQuietNightsAndTheGrandInquisitorsHunt(
      final boolean wanted,
      final boolean quiet,
      final HuntRecord.Phase phase,
      final boolean sends) {
    assertEquals(sends, RaidRules.sends(wanted, quiet, phase));
  }

  @ParameterizedTest(name = "passing {0}, wanted {1}, {2}: called off {3}")
  @CsvSource({
    "false, true, NONE, false",
    "false, false, NONE, true",
    "false, true, WARNING, true",
    "true, false, NONE, false",
    "true, true, WARNING, false"
  })
  void callsOffAGuildRaidOnceItIsNoLongerWanted(
      final boolean passing,
      final boolean wanted,
      final HuntRecord.Phase phase,
      final boolean calledOff) {
    assertEquals(calledOff, RaidRules.callsOff(passing, wanted, phase));
  }

  @ParameterizedTest(name = "housed {0} at {1}: goes {2}")
  @CsvSource({
    "true, 1000, true",
    "true, 12999, true",
    "true, 13000, false",
    "true, 22999, false",
    "true, 23000, true",
    "false, 1000, false",
    "false, 13000, true",
    "false, 37000, true",
    "false, 47000, false"
  })
  void raidsAHouseByDayAndAVampireWithNoHouseByNight(
      final boolean housed, final long time, final boolean goes) {
    assertEquals(goes, RaidRules.goesNow(housed, time));
  }

  @ParameterizedTest(name = "{0} left: beaten {1}")
  @CsvSource({"0, true", "1, false", "12, false"})
  void isBeatenWhenNobodyIsLeft(final int left, final boolean beaten) {
    assertEquals(beaten, RaidRules.beaten(left));
  }

  @ParameterizedTest(name = "{0}: rests {1}")
  @CsvSource({"NONE, true", "WARNING, false", "DUE, false", "VISIT, false"})
  void givesQuietNightsOnlyWhenTheGrandInquisitorIsNotAlreadyComing(
      final HuntRecord.Phase phase, final boolean rests) {
    assertEquals(rests, RaidRules.restsAfter(phase));
  }
}
