package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.NPC;
import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HouseChallengesTest {

  static Stream<Arguments> challenges() {
    return Stream.of(
        Arguments.of(HouseChallenges.NONE),
        Arguments.of(HouseChallenges.NONE.declared(Challenge.declared(PLAYER, 3L))),
        Arguments.of(
            HouseChallenges.NONE.lost(NPC, 19L).declared(Challenge.declared(PLAYER, -2L))));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("challenges")
  @DisplayName("a house's challenges survive a save and load whole")
  void roundTrip(final HouseChallenges challenges) {
    assertEquals(
        challenges,
        HouseChallenges.CODEC
            .parse(
                JsonOps.INSTANCE,
                HouseChallenges.CODEC.encodeStart(JsonOps.INSTANCE, challenges).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "barred until 18, night {0} -> {1}")
  @CsvSource({"10, true", "17, true", "18, false", "40, false"})
  @DisplayName("losing a challenge settles it and bars the loser for a while")
  void lost(final long night, final boolean barred) {
    final HouseChallenges after =
        HouseChallenges.NONE.declared(Challenge.declared(PLAYER, 9L)).lost(PLAYER, 18L);
    assertEquals(Optional.empty(), after.pending());
    assertEquals(barred, after.barred(PLAYER, night));
    assertEquals(false, after.barred(NPC, night));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("challenges")
  @DisplayName("settling a challenge keeps the bars")
  void settled(final HouseChallenges challenges) {
    assertEquals(Optional.empty(), challenges.settled().pending());
    assertEquals(challenges.barredUntil(), challenges.settled().barredUntil());
  }
}
