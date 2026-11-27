package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LeaderlessCampsTest {

  private static final HunterParty.Camp CAMP =
      new HunterParty.Camp(new BlockPos(5, 64, 5), List.of(), List.of(new BlockPos(6, 64, 6)));

  @ParameterizedTest(name = "stay over {0}, chunks loaded {1}, leader here {2}: cleared {3}")
  @CsvSource({
    "true, true, false, true",
    "true, true, true, false",
    "true, false, false, false",
    "false, true, false, false",
    "false, false, true, false",
    "true, false, true, false",
    "false, true, true, false",
    "false, false, false, false"
  })
  void clearsOnlyALoadedCampWithNoLeaderWhenItsNightsRunOut(
      final boolean over, final boolean loaded, final boolean led, final boolean cleared) {
    assertEquals(cleared, LeaderlessCamps.clears(over, loaded, led));
  }

  @ParameterizedTest(name = "leader alive {0}, has a party {1}, camped {2}: holds the camp {3}")
  @CsvSource({
    "true, true, true, true",
    "true, true, false, false",
    "true, false, false, false",
    "false, true, true, false",
    "false, false, false, false"
  })
  void onlyALivingLeaderWhosePartyIsCampedHoldsTheCamp(
      final boolean alive, final boolean hasParty, final boolean camped, final boolean holds) {
    final HunterParty gathered =
        HunterParty.gathered(List.of(UUID.randomUUID()), new BlockPos(0, 64, 0), 0L);
    final HunterParty party = camped ? gathered.pitch(CAMP, 1, 0L, 0L) : gathered;
    assertEquals(
        holds, LeaderlessCamps.holds(alive, hasParty ? Optional.of(party) : Optional.empty()));
  }

  @ParameterizedTest(
      name = "led {0}, village calm {1}, released {2} at time of day {3}: over {4}, released {5}")
  @CsvSource({
    "false, true, false, 6000, true, true",
    "false, true, false, 18000, false, true",
    "false, false, false, 6000, false, false",
    "true, true, false, 6000, false, false",
    "false, true, true, 18000, true, true",
    "false, false, true, 6000, true, true"
  })
  void aLeaderlessGuardCampComesDownOnceItsVillageIsCalm(
      final boolean led,
      final boolean calm,
      final boolean alreadyReleased,
      final long timeOfDay,
      final boolean over,
      final boolean released) {
    final long now = 500_000L;
    final CampStay pitched = CampStay.guarding(new BlockPos(40, 64, -12), 1L, 30_000L);
    final CampStay stay = alreadyReleased ? pitched.released(now - CampStay.NIGHT_LENGTH) : pitched;
    final CampStay kept = LeaderlessCamps.kept(stay, led, calm, now);
    assertEquals(released, kept.released());
    assertEquals(over, kept.over(timeOfDay, now));
  }

  @ParameterizedTest(name = "led {0}, relieved {1}: a passing camp keeps its stay")
  @CsvSource({"false, true", "true, true", "false, false"})
  void aPassingCampIsNeverReleased(final boolean led, final boolean relieved) {
    final CampStay stay = new CampStay(2, 3L, 84_000L);
    assertEquals(stay, LeaderlessCamps.kept(stay, led, relieved, 90_000L));
  }
}
