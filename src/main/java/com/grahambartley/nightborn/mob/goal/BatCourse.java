package com.grahambartley.nightborn.mob.goal;

public enum BatCourse {
  CHASE,
  FLY_ON,
  HOVER,
  LAND;

  public static BatCourse decide(
      final boolean chasing,
      final boolean hasTarget,
      final double courseDistance,
      final double landRange,
      final boolean held,
      final boolean groundBelow) {
    if (chasing) {
      return CHASE;
    }
    final boolean hasCourse = !Double.isNaN(courseDistance);
    if (!hasTarget && hasCourse && courseDistance > landRange) {
      return FLY_ON;
    }
    if (held) {
      return hasTarget ? CHASE : HOVER;
    }
    if (groundBelow) {
      return LAND;
    }
    if (hasTarget) {
      return CHASE;
    }
    return hasCourse ? FLY_ON : HOVER;
  }
}
