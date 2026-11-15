package com.grahambartley.nightborn.coffin;

import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;

public final class RestRules {

  public static final String VAMPIRE_IN_BED = "nightborn.coffin.vampire_in_bed";
  public static final String HUMAN_IN_COFFIN = "nightborn.coffin.human_in_coffin";
  public static final String NOT_HERE = "nightborn.coffin.not_here";
  public static final String NOT_NOW = "nightborn.coffin.not_now";
  public static final String TOO_FAR = "nightborn.coffin.too_far";
  public static final String OBSTRUCTED = "nightborn.coffin.obstructed";
  public static final String NOT_SAFE = "block.minecraft.bed.not_safe";
  public static final String OCCUPIED = "nightborn.coffin.occupied";

  private RestRules() {}

  public static Optional<String> refusal(final boolean vampire, final boolean coffin) {
    if (vampire && !coffin) {
      return Optional.of(VAMPIRE_IN_BED);
    }
    if (!vampire && coffin) {
      return Optional.of(HUMAN_IN_COFFIN);
    }
    return Optional.empty();
  }

  public static boolean wrongTime(final boolean coffin, final boolean day) {
    return coffin != day;
  }

  public static Optional<String> message(final SleepFailureReason reason) {
    return switch (reason) {
      case NOT_POSSIBLE_HERE -> Optional.of(NOT_HERE);
      case NOT_POSSIBLE_NOW -> Optional.of(NOT_NOW);
      case TOO_FAR_AWAY -> Optional.of(TOO_FAR);
      case OBSTRUCTED -> Optional.of(OBSTRUCTED);
      case NOT_SAFE -> Optional.of(NOT_SAFE);
      case OTHER_PROBLEM -> Optional.empty();
    };
  }
}
