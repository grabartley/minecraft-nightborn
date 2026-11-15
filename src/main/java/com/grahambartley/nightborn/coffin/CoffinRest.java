package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import java.util.UUID;

public enum CoffinRest {
  CLAIM,
  RETURN,
  BORROW;

  public static CoffinRest of(final Optional<UUID> owner, final UUID sleeper) {
    if (owner.isEmpty()) {
      return CLAIM;
    }
    return owner.get().equals(sleeper) ? RETURN : BORROW;
  }

  public boolean setsRespawn() {
    return this != BORROW;
  }
}
