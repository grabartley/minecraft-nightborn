package com.grahambartley.nightborn.inquisitor;

public enum InquisitorEnding {
  NONE,
  WON,
  EMPTY;

  public static InquisitorEnding of(
      final int foesInside, final int foesStanding, final long emptyTicks, final long limit) {
    if (foesInside > 0) {
      return NONE;
    }
    if (foesStanding <= 0) {
      return WON;
    }
    return emptyTicks >= limit ? EMPTY : NONE;
  }
}
