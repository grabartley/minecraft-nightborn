package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.hunt.HuntRecord;
import com.grahambartley.nightborn.hunt.HuntRules;

public final class RaidRules {

  public enum End {
    STAKED,
    BEATEN,
    DUSK,
    CALLED_OFF
  }

  private RaidRules() {}

  public static boolean wanted(final HuntRules.Level level, final boolean knownFace) {
    return level == HuntRules.Level.RAID || knownFace;
  }

  public static boolean sends(
      final boolean wanted, final boolean quiet, final HuntRecord.Phase phase) {
    return wanted && !quiet && phase == HuntRecord.Phase.NONE;
  }

  public static boolean callsOff(
      final boolean passing, final boolean wanted, final HuntRecord.Phase phase) {
    return !passing && (!wanted || phase != HuntRecord.Phase.NONE);
  }

  public static boolean goesNow(final boolean housed, final long timeOfDay) {
    return housed != NightTime.isNight(false, timeOfDay);
  }

  public static boolean beaten(final int left) {
    return left <= 0;
  }
}
