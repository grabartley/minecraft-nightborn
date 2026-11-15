package com.grahambartley.nightborn.levitation;

public final class LevitationRules {

  private LevitationRules() {}

  public static boolean mayLevitate(
      final boolean vampire, final int weakened, final boolean downed) {
    return vampire && weakened <= 0 && !downed;
  }
}
