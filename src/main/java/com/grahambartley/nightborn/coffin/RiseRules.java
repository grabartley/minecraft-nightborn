package com.grahambartley.nightborn.coffin;

import java.util.OptionalLong;

public final class RiseRules {

  public static final int RISE_TICKS = 30;
  public static final int STEP_OUT_TICKS = 20;

  public enum Wake {
    BEGIN,
    WAIT,
    WAKE
  }

  private RiseRules() {}

  public static int ticks(final boolean standing) {
    return standing ? STEP_OUT_TICKS : RISE_TICKS;
  }

  public static Wake wake(
      final boolean held, final boolean burning, final OptionalLong until, final long now) {
    if (!held) {
      return Wake.WAKE;
    }
    if (until.isEmpty()) {
      return burning ? Wake.WAKE : Wake.BEGIN;
    }
    return now < until.getAsLong() ? Wake.WAIT : Wake.WAKE;
  }
}
