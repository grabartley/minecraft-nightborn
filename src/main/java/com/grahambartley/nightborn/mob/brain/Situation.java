package com.grahambartley.nightborn.mob.brain;

import org.jetbrains.annotations.Nullable;

public record Situation(Intent intent, Self self, @Nullable Target target, Place place) {

  public Situation(final Intent intent, final Self self, @Nullable final Target target) {
    this(intent, self, target, Place.UNKNOWN);
  }

  public record Self(float health, int blood, boolean inForm, boolean sunlit, boolean downed) {

    public static Self of(final float health, final int blood) {
      return new Self(health, blood, false, false, false);
    }
  }

  public record Target(
      double distance,
      boolean inSight,
      float health,
      boolean windingUp,
      boolean running,
      boolean slowed,
      boolean airborne,
      boolean vampire,
      boolean hunter) {

    public Target(final double distance, final boolean inSight, final float health) {
      this(distance, inSight, health, false, false, false, false, false, false);
    }
  }

  public record Place(double spotDistance) {

    public static final Place UNKNOWN = new Place(Double.NaN);
  }
}
