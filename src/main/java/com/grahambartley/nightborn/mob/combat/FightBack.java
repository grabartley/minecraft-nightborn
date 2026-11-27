package com.grahambartley.nightborn.mob.combat;

public final class FightBack {

  private FightBack() {}

  public static boolean answers(
      final boolean struckBy,
      final boolean helpless,
      final boolean fleeing,
      final boolean guild,
      final boolean byGuild) {
    return struckBy && !helpless && !fleeing && !(guild && byGuild);
  }
}
