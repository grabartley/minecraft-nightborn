package com.grahambartley.nightborn.enchant;

public record BloodlettingCount(int blows, boolean bleeds) {

  public static BloodlettingCount after(final int blowsSoFar, final int blowsPerBlood) {
    final int blows = Math.max(0, blowsSoFar) + 1;
    if (blowsPerBlood > 0 && blows >= blowsPerBlood) {
      return new BloodlettingCount(0, true);
    }
    return new BloodlettingCount(blows, false);
  }
}
