package com.grahambartley.nightborn.familiar;

public final class CareRules {

  public enum Care {
    PAY,
    MOOD,
    NONE
  }

  private CareRules() {}

  public static Care choose(
      final boolean owner,
      final boolean mainHand,
      final boolean heldEmerald,
      final boolean heldEmpty,
      final boolean offHandEmerald) {
    if (!owner) {
      return Care.NONE;
    }
    if (heldEmerald) {
      return Care.PAY;
    }
    return mainHand && heldEmpty && !offHandEmerald ? Care.MOOD : Care.NONE;
  }
}
