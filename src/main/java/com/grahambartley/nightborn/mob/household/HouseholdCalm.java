package com.grahambartley.nightborn.mob.household;

public final class HouseholdCalm {

  private HouseholdCalm() {}

  public static boolean calm(
      final boolean sleeping,
      final boolean hunting,
      final boolean downed,
      final boolean bat,
      final boolean out,
      final boolean bedtime) {
    return !sleeping && !hunting && !downed && !bat && !out && !bedtime;
  }

  public static boolean free(
      final boolean alive,
      final boolean sleeping,
      final boolean hunting,
      final boolean engaged,
      final boolean grieving) {
    return alive && !sleeping && !hunting && !engaged && !grieving;
  }
}
