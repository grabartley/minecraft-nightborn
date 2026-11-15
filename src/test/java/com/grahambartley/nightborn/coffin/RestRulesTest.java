package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RestRulesTest {

  static Stream<Arguments> sleepers() {
    return Stream.of(
        Arguments.of(true, true, Optional.empty()),
        Arguments.of(true, false, Optional.of(RestRules.VAMPIRE_IN_BED)),
        Arguments.of(false, true, Optional.of(RestRules.HUMAN_IN_COFFIN)),
        Arguments.of(false, false, Optional.empty()));
  }

  @ParameterizedTest(name = "vampire {0}, coffin {1}")
  @MethodSource("sleepers")
  @DisplayName("vampires rest only in coffins and humans only in beds")
  void refusesTheWrongPlace(
      final boolean vampire, final boolean coffin, final Optional<String> refusal) {
    assertEquals(refusal, RestRules.refusal(vampire, coffin));
  }

  static Stream<Arguments> times() {
    return Stream.of(
        Arguments.of(true, true, false),
        Arguments.of(true, false, true),
        Arguments.of(false, true, true),
        Arguments.of(false, false, false));
  }

  @ParameterizedTest(name = "coffin {0}, day {1}")
  @MethodSource("times")
  @DisplayName("a coffin is for the day and a bed is for the night")
  void knowsTheWrongTime(final boolean coffin, final boolean day, final boolean wrong) {
    assertEquals(wrong, RestRules.wrongTime(coffin, day));
  }

  static Stream<Arguments> reasons() {
    return Stream.of(
        Arguments.of(SleepFailureReason.NOT_POSSIBLE_HERE, Optional.of(RestRules.NOT_HERE)),
        Arguments.of(SleepFailureReason.NOT_POSSIBLE_NOW, Optional.of(RestRules.NOT_NOW)),
        Arguments.of(SleepFailureReason.TOO_FAR_AWAY, Optional.of(RestRules.TOO_FAR)),
        Arguments.of(SleepFailureReason.OBSTRUCTED, Optional.of(RestRules.OBSTRUCTED)),
        Arguments.of(SleepFailureReason.NOT_SAFE, Optional.of(RestRules.NOT_SAFE)),
        Arguments.of(SleepFailureReason.OTHER_PROBLEM, Optional.empty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("reasons")
  @DisplayName("every refusal from the game speaks of a coffin, or stays quiet")
  void namesEachRefusal(final SleepFailureReason reason, final Optional<String> message) {
    assertEquals(message, RestRules.message(reason));
  }
}
