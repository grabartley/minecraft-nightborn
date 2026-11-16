package com.grahambartley.nightborn.mob.look;

import java.util.Locale;

public enum VampireVoice {
  AMBIENT,
  HURT,
  DEATH,
  HISS,
  TALK,
  ARGUE;

  static final float AVERAGE_HEIGHT = 1.75F;
  static final float PITCH_PER_BLOCK = 0.6F;
  static final float LOWEST = 0.8F;
  static final float HIGHEST = 1.2F;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static float pitch(final float height) {
    final float pitch = 1.0F + (AVERAGE_HEIGHT - height) * PITCH_PER_BLOCK;
    return Math.max(LOWEST, Math.min(HIGHEST, pitch));
  }
}
