package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

  @ParameterizedTest(name = "challenger online on nights {0} -> house lost on night {1}")
  @CsvSource({
    "11 12 13 14 15, 14",
    "11 12 13, 14",
    "11 13 15 16, 16",
    "12 12 12 13, -1",
    "10 11, -1"
  })
  @DisplayName(
      "reviewed night by night, the lord loses once the challenger has been there three whole nights")
  void reviewsNightByNight(final String online, final long lostOn) {
    final Set<Long> present =
        Arrays.stream(online.split(" ")).map(Long::parseLong).collect(Collectors.toSet());
    Challenge challenge = Challenge.declared(PLAYER, 10L);
    long lost = -1L;
    for (long night = 10L; night <= 20L && lost < 0; night++) {
      for (int review = 0; review < 3 && lost < 0; review++) {
        final ChallengeRules.Review step =
            ChallengeRules.review(
                challenge, night, present.contains(night), 3, Optional.empty(), 6);
        challenge = step.challenge();
        if (step.outcome() == ChallengeRules.Outcome.UNANSWERED) {
          lost = night;
        }
      }
    }
    assertEquals(lostOn, lost);
  }

  @ParameterizedTest(name = "pending {0}, operator {1} -> {2}")
  @CsvSource({"false, false, true", "true, false, false", "true, true, true", "false, true, true"})
  @DisplayName("a lord cannot disband a challenged house, but an operator can")
  void mayDisband(final boolean pending, final boolean operator, final boolean may) {
    assertEquals(may, ChallengeRules.mayDisband(pending, operator));
  }
}
