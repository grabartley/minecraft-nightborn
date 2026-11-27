package com.grahambartley.nightborn.dismember;

public enum SeverAim {
  LINE_OF_SIGHT,
  IMPACT,
  MOB_WEIGHTS,
  RANDOM_LIMB;

  public static SeverAim of(
      final boolean blast,
      final boolean direct,
      final boolean projectile,
      final boolean playerAttacker) {
    if (blast) {
      return RANDOM_LIMB;
    }
    if (projectile) {
      return IMPACT;
    }
    if (!direct) {
      return RANDOM_LIMB;
    }
    return playerAttacker ? LINE_OF_SIGHT : MOB_WEIGHTS;
  }
}
