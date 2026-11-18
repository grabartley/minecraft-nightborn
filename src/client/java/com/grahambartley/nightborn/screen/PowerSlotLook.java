package com.grahambartley.nightborn.screen;

public enum PowerSlotLook {
  EMPTY,
  FILLED,
  LIFTED,
  TARGET;

  public static PowerSlotLook of(final boolean filled, final boolean lifted, final boolean target) {
    if (target) {
      return TARGET;
    }
    if (lifted) {
      return LIFTED;
    }
    return filled ? FILLED : EMPTY;
  }
}
