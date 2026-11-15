package com.grahambartley.nightborn.coffin;

public enum CoffinUse {
  REFUSE_OCCUPIED,
  TOGGLE_LID,
  REST;

  public static CoffinUse of(
      final boolean occupied, final boolean sneaking, final boolean vampire) {
    if (occupied) {
      return REFUSE_OCCUPIED;
    }
    return sneaking || !vampire ? TOGGLE_LID : REST;
  }
}
