package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.hunt.HuntRecord;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PassingRaidTest {

  @ParameterizedTest(name = "pitched {0}, night {1}: looks {2}")
  @CsvSource({
    "false, false, true",
    "true, false, false",
    "false, true, false",
    "true, true, false"
  })
  void looksForHousesOnlyWhileOnTheMoveByDay(
      final boolean pitched, final boolean night, final boolean looks) {
    assertEquals(looks, PassingRaid.looks(pitched, night));
  }

  @ParameterizedTest(
      name =
          "same world {0}, {1} away, sight {2}, online {3}, vampire {4}, resting {5}, underway {6}: {7}")
  @CsvSource({
    "true, 20.0, 32, true, true, false, false, true",
    "true, 32.0, 32, true, true, false, false, true",
    "true, 32.5, 32, true, true, false, false, false",
    "true, 40.0, 48, true, true, false, false, true",
    "false, 20.0, 32, true, true, false, false, false",
    "true, 20.0, 32, false, false, false, false, false",
    "true, 20.0, 32, true, false, false, false, false",
    "true, 20.0, 32, true, true, true, false, false",
    "true, 20.0, 32, true, true, false, true, false"
  })
  void raidsAHouseInSightOfAVampireWhoIsNotResting(
      final boolean sameWorld,
      final double distance,
      final int sight,
      final boolean online,
      final boolean vampire,
      final boolean resting,
      final boolean underway,
      final boolean raids) {
    assertEquals(
        raids, PassingRaid.targets(sameWorld, distance, sight, online, vampire, resting, underway));
  }

  @ParameterizedTest(name = "quiet until night {0}, {1}: resting {2}")
  @CsvSource({
    "-9223372036854775808, NONE, false",
    "5, NONE, true",
    "-9223372036854775808, WARNING, true",
    "-9223372036854775808, VISIT, true"
  })
  void leavesAloneAVampireInQuietNightsOrUnderTheInquisitorsHunt(
      final long quietUntil, final HuntRecord.Phase phase, final boolean resting) {
    final HuntRecord record =
        new HuntRecord(
            20.0,
            phase,
            0,
            phase == HuntRecord.Phase.VISIT
                ? Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                : Optional.empty(),
            quietUntil,
            Optional.empty());
    assertEquals(resting, PassingRaid.resting(record, 1000L));
  }
}
