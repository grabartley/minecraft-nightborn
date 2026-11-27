package com.grahambartley.nightborn.guild.rite;

public final class RiteSteps {

  public static final int NOT_YET = -1;

  public enum Way {
    SEARCHING,
    LOADING,
    READY,
    NONE,
    UNSAFE
  }

  public enum Step {
    CAST,
    FAIL_NONE,
    FAIL_UNSAFE,
    FAIL_FADED,
    BEGIN_LEAVE,
    LEAVE,
    DEPART
  }

  private RiteSteps() {}

  public static Step at(
      final int age, final int leftAt, final GuildRite.Timing timing, final Way way) {
    if (leftAt != NOT_YET) {
      return age - leftAt >= timing.leaveTicks() ? Step.DEPART : Step.LEAVE;
    }
    if (age < timing.castTicks()) {
      return Step.CAST;
    }
    final boolean late = age >= timing.castTicks() + timing.waitTicks();
    return switch (way) {
      case NONE -> Step.FAIL_NONE;
      case UNSAFE -> Step.FAIL_UNSAFE;
      case READY -> age % timing.castTicks() == 0 ? Step.BEGIN_LEAVE : Step.CAST;
      case SEARCHING, LOADING -> late ? Step.FAIL_FADED : Step.CAST;
    };
  }

  public static boolean repeats(final int age, final int leftAt, final GuildRite.Timing timing) {
    return leftAt == NOT_YET && age > 0 && age % timing.castTicks() == 0;
  }

  public static int castTick(final int age, final GuildRite.Timing timing) {
    return age % timing.castTicks();
  }

  public static boolean settles(final int sinceArrival, final GuildRite.Timing timing) {
    return sinceArrival == timing.settleTicks();
  }

  public static boolean over(
      final int sinceArrival, final GuildRite.Timing timing, final boolean sheltered) {
    return sinceArrival >= timing.settleTicks() && !sheltered;
  }
}
