package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public final class CoffinGuard {

  private CoffinGuard() {}

  public static boolean mayBreak(
      final Optional<UUID> owner,
      @Nullable final UUID breaker,
      final boolean creative,
      final boolean operator) {
    return owner.isEmpty() || creative || operator || owner.get().equals(breaker);
  }
}
