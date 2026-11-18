package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.LORD;
import static com.grahambartley.nightborn.house.HouseFixtures.NPC;
import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static com.grahambartley.nightborn.house.HouseFixtures.STRANGER;
import static com.grahambartley.nightborn.house.HouseFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HouseSuccessionTest {

  static Stream<Arguments> heirs() {
    final House house =
        House.founded(LORD, at(0, 64, 0))
            .withPlayer(PLAYER)
            .withPlayer(STRANGER)
            .withNpc(NPC)
            .ranked(PLAYER, 1)
            .ranked(STRANGER, 0)
            .ranked(NPC, 3);
    return Stream.of(
        Arguments.of(house, PLAYER, true),
        Arguments.of(house, STRANGER, false),
        Arguments.of(house, NPC, false),
        Arguments.of(house, LORD, false));
  }

  @ParameterizedTest(name = "{1} -> {2}")
  @MethodSource("heirs")
  @DisplayName("a house passes only to a player member who is an adult or older")
  void mayInherit(final House house, final UUID member, final boolean may) {
    assertEquals(may, HouseSuccession.mayInherit(house, member));
  }
}
