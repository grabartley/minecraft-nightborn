package com.grahambartley.nightborn.mob;

public final class VampireFeud {

  private VampireFeud() {}

  public static boolean answers(
      final boolean struckBy,
      final int strikerStage,
      final int ownStage,
      final boolean answersEveryStriker) {
    return struckBy && (answersEveryStriker || strikerStage >= ownStage);
  }

  public static boolean angryAt(final boolean vampire, final boolean fighting) {
    return !vampire || fighting;
  }
}
