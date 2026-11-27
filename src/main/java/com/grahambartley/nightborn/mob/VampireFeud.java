package com.grahambartley.nightborn.mob;

public final class VampireFeud {

  private VampireFeud() {}

  public static boolean fights(
      final boolean quarry, final boolean targetVampire, final boolean answersStrike) {
    return quarry || !targetVampire || answersStrike;
  }

  public static boolean angryAt(final boolean vampire, final boolean fighting) {
    return !vampire || fighting;
  }
}
