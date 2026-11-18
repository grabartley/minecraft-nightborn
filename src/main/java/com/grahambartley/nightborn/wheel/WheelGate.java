package com.grahambartley.nightborn.wheel;

public final class WheelGate {

  private WheelGate() {}

  public static boolean opens(
      final boolean mainHand, final boolean keepsOwnUse, final boolean spectator) {
    return mainHand && !keepsOwnUse && !spectator;
  }
}
