package com.grahambartley.nightborn.decor;

import java.util.Optional;

public enum RackUse {
  HANG,
  TAKE;

  public static Optional<RackUse> decide(
      final boolean holdsWeapon, final boolean emptyHand, final boolean hookFree) {
    if (holdsWeapon && hookFree) {
      return Optional.of(HANG);
    }
    return emptyHand && !hookFree ? Optional.of(TAKE) : Optional.empty();
  }
}
