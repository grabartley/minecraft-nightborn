package com.grahambartley.nightborn.familiar;

public final class CareRules {

  public enum Care {
    PAY,
    OTHER_HAND,
    NONE
  }

  private CareRules() {}

  public static Care choose(
      final boolean owner,
      final boolean mainHand,
      final boolean heldEmerald,
      final boolean offHandEmerald) {
    if (!owner) {
      return Care.NONE;
    }
    if (heldEmerald) {
      return Care.PAY;
    }
    return mainHand && offHandEmerald ? Care.OTHER_HAND : Care.NONE;
  }
}
