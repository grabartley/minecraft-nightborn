package com.grahambartley.nightborn.guild;

public final class DrawnWeapon {

  static final int LINGER_TICKS = 100;

  private int left;

  public boolean tick(final boolean hunting) {
    if (hunting) {
      left = LINGER_TICKS;
    } else if (left > 0) {
      left--;
    }
    return left > 0;
  }
}
