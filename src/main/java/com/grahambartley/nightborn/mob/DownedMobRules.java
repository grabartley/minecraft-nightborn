package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.downed.DownedState;
import org.jetbrains.annotations.Nullable;

public final class DownedMobRules {

  private DownedMobRules() {}

  public static boolean helpless(@Nullable final DownedState state) {
    return state != null && state.isDowned();
  }

  public static boolean flees(@Nullable final DownedState state) {
    return state != null && !state.isDowned();
  }
}
