package com.grahambartley.nightborn.faith;

public final class KindleRules {

  private KindleRules() {}

  public static boolean affords(final float health, final float cost) {
    return health > cost;
  }

  public static boolean sets(final boolean vampire, final boolean weaponBlow) {
    return vampire && weaponBlow;
  }

  public static float heal(final float amount, final boolean seared, final boolean onFire) {
    return seared && onFire ? 0.0F : amount;
  }
}
