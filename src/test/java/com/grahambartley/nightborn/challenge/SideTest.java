package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SideTest {

  @ParameterizedTest(name = "vampire {0}, hunter {1} is {2}")
  @CsvSource({
    "true, false, VAMPIRE",
    "false, true, HUNTER",
    "false, false,",
    "true, true, VAMPIRE"
  })
  void aPlayerStandsOnOneSideOrNone(final boolean vampire, final boolean hunter, final Side side) {
    assertEquals(Optional.ofNullable(side), Side.of(vampire, hunter));
  }

  @ParameterizedTest(name = "{0} holds a player on {1}: {2}")
  @CsvSource({
    "VAMPIRE, VAMPIRE, true",
    "VAMPIRE, HUNTER, false",
    "VAMPIRE, , false",
    "HUNTER, HUNTER, true",
    "ANYONE, , true",
    "ANYONE, VAMPIRE, true"
  })
  void anyoneHoldsEveryone(final Side side, final Side player, final boolean holds) {
    assertEquals(holds, side.holds(Optional.ofNullable(player)));
  }
}
