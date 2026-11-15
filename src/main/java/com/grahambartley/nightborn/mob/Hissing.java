package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.WeakenedSources;
import java.util.List;

public final class Hissing {

  static final List<String> WARDS =
      List.of(Wards.STRINGS, Wards.TOUCH, Wards.CRUCIFIX, Wards.SHAPE);
  static final int TICKS_PER_SECOND = 20;

  private Hissing() {}

  public static boolean ready(final long now, final long nextAllowed) {
    return now >= nextAllowed;
  }

  public static long nextAllowed(final long now, final int cooldownSeconds) {
    return now + (long) cooldownSeconds * TICKS_PER_SECOND;
  }

  public static boolean warded(final WeakenedSources sources) {
    return WARDS.stream().anyMatch(id -> sources.strength(id) > 0);
  }
}
