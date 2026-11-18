package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChallengeRulesTest {

  @ParameterizedTest(name = "member {0}, stage {1} vs {2}, pending {3}, barred {4} -> {5}")
  @CsvSource({
    "true, 2, 2, false, false, DECLARED",
    "true, 3, 2, false, false, DECLARED",
    "true, 1, 2, false, false, TOO_YOUNG",
    "false, 3, 2, false, false, NOT_MEMBER",
    "true, 2, 2, true, false, PENDING",
    "true, 2, 2, false, true, BARRED"
  })
  @DisplayName("only a member of the lord's stage or older may challenge, once at a time")
  void declare(
      final boolean member,
      final int stage,
      final int lordStage,
      final boolean pending,
      final boolean barred,
      final ChallengeRules.Verdict verdict) {
    assertEquals(verdict, ChallengeRules.declare(member, stage, lordStage, pending, barred));
  }

  @ParameterizedTest(name = "offered {0} nights, last {1}, now {2}, apart {3} -> {4}")
  @CsvSource({
    "0, 10, 10, , WAITING",
    "2, 12, 13, , WAITING",
    "3, 13, 13, , WAITING",
    "3, 13, 14, , UNANSWERED",
    "3, 13, 30, 2.0, UNANSWERED",
    "0, 10, 30, , WAITING",
    "1, 11, 11, 5.9, MEET",
    "1, 11, 11, 6.0, MEET",
    "1, 11, 11, 6.1, WAITING"
  })
  @DisplayName(
      "a lord loses the house only after three whole nights the challenger could be met on")
  void outcome(
      final int offered,
      final long lastOffered,
      final long night,
      final Double apart,
      final ChallengeRules.Outcome outcome) {
    assertEquals(
        outcome,
        ChallengeRules.outcome(
            new Challenge(PLAYER, 10L, offered, lastOffered),
            night,
            3,
            Optional.ofNullable(apart),
            6));
  }

  @ParameterizedTest(name = "lord standing {0}, challenger standing {1} -> lord loses {2}")
  @CsvSource({"true, true,", "false, false,", "false, true, true", "true, false, false"})
  @DisplayName("a fighter who leaves the circle loses, and if both leave nobody does")
  void absence(final boolean lord, final boolean challenger, final Boolean lordLoses) {
    assertEquals(
        Optional.ofNullable(lordLoses), ChallengeRules.lordLosesByAbsence(lord, challenger));
  }

  @ParameterizedTest(name = "lost on night {0} with {1} nights -> barred until {2}")
  @CsvSource({"10, 7, 18", "0, 0, 1"})
  @DisplayName("a lost challenge cannot be made again until the nights have passed")
  void barredUntil(final long night, final int repeat, final long until) {
    assertEquals(until, ChallengeRules.barredUntil(night, repeat));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "DECLARED, nightborn.house.challenge.declared",
    "TOO_YOUNG, nightborn.house.challenge.too_young"
  })
  @DisplayName("each verdict names its own line")
  void key(final ChallengeRules.Verdict verdict, final String key) {
    assertEquals(key, verdict.key());
  }
}
