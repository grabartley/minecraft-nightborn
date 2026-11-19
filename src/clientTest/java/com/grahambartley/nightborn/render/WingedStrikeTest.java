package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WingedStrikeTest {

  @ParameterizedTest(name = "begun {0} while playing {1} -> {2}")
  @CsvSource({
    "ROAR, , ROAR",
    "CLAW, , CLAW",
    ", , ",
    ", CLAW, CLAW",
    "ROAR, CLAW, ROAR",
    "HURT, CLAW, HURT",
    "CLAW, CLAW, CLAW",
    "CLAW, ROAR, ROAR",
    "HURT, ROAR, ROAR",
    "CLAW, HURT, HURT"
  })
  @DisplayName("a new strike cuts in only over a lesser one, and a roar is never cut short")
  void next(final WingedStrike begun, final WingedStrike playing, final WingedStrike expected) {
    assertEquals(
        Optional.ofNullable(expected),
        WingedStrike.next(Optional.ofNullable(begun), Optional.ofNullable(playing)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(WingedStrike.class)
  @DisplayName("every strike is recognised again from the clip it plays")
  void playing(final WingedStrike strike) {
    assertEquals(Optional.of(strike), WingedStrike.playing(strike.clip()));
  }
}
