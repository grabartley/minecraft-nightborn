package com.grahambartley.nightborn.house;

import java.util.Locale;
import java.util.Optional;

public final class ChallengeRules {

  public enum Verdict {
    DECLARED,
    NOT_MEMBER,
    TOO_YOUNG,
    PENDING,
    BARRED;

    public String key() {
      return "nightborn.house.challenge." + name().toLowerCase(Locale.ROOT);
    }
  }

  public enum Outcome {
    WAITING,
    MEET,
    UNANSWERED
  }

  private ChallengeRules() {}

  public static Verdict declare(
      final boolean member,
      final int challengerStage,
      final int lordStage,
      final boolean pending,
      final boolean barred) {
    if (!member) {
      return Verdict.NOT_MEMBER;
    }
    if (challengerStage < lordStage) {
      return Verdict.TOO_YOUNG;
    }
    if (pending) {
      return Verdict.PENDING;
    }
    return barred ? Verdict.BARRED : Verdict.DECLARED;
  }

  public static Outcome outcome(
      final Challenge challenge,
      final long night,
      final int answerNights,
      final Optional<Double> apart,
      final int reach) {
    if (night - challenge.night() > answerNights) {
      return Outcome.UNANSWERED;
    }
    return apart.filter(distance -> distance <= reach).isPresent() ? Outcome.MEET : Outcome.WAITING;
  }

  public static long barredUntil(final long night, final int repeatNights) {
    return night + repeatNights + 1;
  }
}
