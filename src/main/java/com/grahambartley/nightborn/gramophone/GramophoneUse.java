package com.grahambartley.nightborn.gramophone;

import java.util.Optional;

public enum GramophoneUse {
  INSERT,
  EJECT;

  public static Optional<GramophoneUse> decide(final boolean hasRecord, final boolean holdsDisc) {
    if (hasRecord) {
      return Optional.of(EJECT);
    }
    return holdsDisc ? Optional.of(INSERT) : Optional.empty();
  }
}
