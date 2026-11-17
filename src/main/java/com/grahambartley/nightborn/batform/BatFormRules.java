package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.weakened.WeakenedSources;
import java.util.List;

public final class BatFormRules {

  private BatFormRules() {}

  public static boolean forcedOut(
      final boolean directSun,
      final boolean downed,
      final boolean warded,
      final int blood,
      final boolean ownsForm) {
    return directSun || downed || warded || blood <= 0 || !ownsForm;
  }

  public static boolean warded(final WeakenedSources sources, final List<String> forcing) {
    return forcing.stream().anyMatch(source -> sources.strength(source) > 0);
  }

  public static boolean creativeFlight(final boolean allowFlying, final boolean bat) {
    return allowFlying && !bat;
  }

  public static boolean perched(final boolean onGround, final boolean floorBelow) {
    return onGround && floorBelow;
  }

  public static boolean heavyHit(final float damage, final int heavyDamage) {
    return damage >= heavyDamage;
  }
}
