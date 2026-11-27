package com.grahambartley.nightborn.mob.combat;

public final class FightBack {

  private FightBack() {}

  public static boolean vampireAnswers(
      final boolean struckBy, final boolean helpless, final boolean fleeing) {
    return struckBy && !helpless && !fleeing;
  }

  public static boolean guildAnswers(final boolean struckBy, final boolean byGuild) {
    return struckBy && !byGuild;
  }
}
