package com.grahambartley.nightborn.mob.brain;

import org.jetbrains.annotations.Nullable;

public record Situation(Intent intent, Self self, @Nullable Target target) {

  public record Self(float health, int blood, boolean inForm, boolean sunlit, boolean downed) {

    public static Self of(final float health, final int blood) {
      return new Self(health, blood, false, false, false);
    }
  }

  public record Target(double distance, boolean inSight, float health) {}
}
