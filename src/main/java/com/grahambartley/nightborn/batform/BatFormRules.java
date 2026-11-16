package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.ammo.GarlicFumesEffect;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.WeakenedSources;
import java.util.List;

public final class BatFormRules {

  static final List<String> FORCING_SOURCES =
      List.of(
          Wards.STRINGS, Wards.TOUCH, GarlicFumesEffect.WEAKENED_SOURCE, HolyWater.WEAKENED_SOURCE);

  private BatFormRules() {}

  public static boolean forcedOut(
      final boolean directSun,
      final boolean downed,
      final boolean warded,
      final int blood,
      final boolean ownsForm) {
    return directSun || downed || warded || blood <= 0 || !ownsForm;
  }

  public static boolean warded(final WeakenedSources sources) {
    return FORCING_SOURCES.stream().anyMatch(source -> sources.strength(source) > 0);
  }

  public static boolean heavyHit(final float damage, final int heavyDamage) {
    return damage >= heavyDamage;
  }
}
