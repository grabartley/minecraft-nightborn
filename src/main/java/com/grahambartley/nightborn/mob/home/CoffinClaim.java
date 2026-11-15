package com.grahambartley.nightborn.mob.home;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public final class CoffinClaim {

  private CoffinClaim() {}

  public static boolean keeps(
      final boolean coffinHead, @Nullable final UUID owner, final UUID vampire) {
    return coffinHead && vampire.equals(owner);
  }

  public static boolean mayClaim(final boolean coffinHead, @Nullable final UUID owner) {
    return coffinHead && owner == null;
  }
}
