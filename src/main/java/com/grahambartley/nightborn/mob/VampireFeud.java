package com.grahambartley.nightborn.mob;

public final class VampireFeud {

  private VampireFeud() {}

  public static boolean fights(
      final boolean quarry, final boolean targetVampire, final boolean struckBy) {
    return quarry || !targetVampire || struckBy;
  }

  public static boolean angryAt(final boolean vampire, final boolean fighting) {
    return !vampire || fighting;
  }
}
