package com.grahambartley.nightborn.batform;

public enum BatEffort {
  NONE,
  RESTING,
  FLYING,
  SPRINTING;

  public static BatEffort of(final boolean bat, final boolean resting, final boolean sprinting) {
    if (!bat) {
      return NONE;
    }
    if (resting) {
      return RESTING;
    }
    return sprinting ? SPRINTING : FLYING;
  }
}
